package nd.esp.service.lifecycle.daos.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableList;
import nd.esp.service.lifecycle.annotations.AutoIncrement;
import nd.esp.service.lifecycle.annotations.Column;
import nd.esp.service.lifecycle.annotations.PrimaryKey;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public class BaseDao<T> {
    
    private Logger logger = Logger.getLogger(BaseDao.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     *  
     * <p>Description: Set model field's value with column value in database. </p>
     * <p>Create Time: 2015年7月10日   </p>
     * <p>Create author: Jawinton </p>
     * @param set
     * @param clazz
     * @return
     * @throws SQLException
     */
    public T setModelField(ResultSet set, Class<T> clazz) {
        T model;
        try {
            model = clazz.newInstance();
            for (Field field : getDeclaredFieldsIncludeSuperClasses(clazz)) {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(Column.class))
                    continue;
                
                try {
                    String columnName = field.getAnnotation(Column.class).name();
                    Class<?> fieldType = field.getType();
                    if (Integer.class == fieldType || int.class == fieldType) {
                        field.set(model, set.getInt(columnName));
                    } else if (Long.class == fieldType || long.class == fieldType) {
                        field.set(model, set.getLong(columnName));
                    } else if (String.class == fieldType) {
                        field.set(model, set.getString(columnName));
                    } else if (Timestamp.class == fieldType) {
                        field.set(model, set.getTimestamp(columnName));
                    } else if (Collection.class == fieldType) {
                        String stringArray = set.getString(columnName);
                        field.set(model, JSON.parse(stringArray));
//                        field.set(model, set.getArray(columnName));
                    } else if (Boolean.class == fieldType || boolean.class == fieldType) {
                        field.set(model, set.getBoolean(columnName));
                    } else if (Float.class == fieldType || float.class == fieldType) {
                        field.set(model, set.getFloat(columnName));
                    } else if (Double.class == fieldType || double.class == fieldType) {
                        field.set(model, set.getDouble(columnName));
                    } else if (byte[].class == fieldType || Byte[].class == fieldType) {
                        field.set(model, set.getBytes(columnName));
                    } else if (Date.class == fieldType) {
                        field.set(model, set.getDate(columnName));
                    } else if (Byte.class == fieldType  || byte.class == fieldType) {
                        field.set(model, set.getByte(columnName));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // continue to set other field.
                    logger.warn("setModelField-IllegalArgument: " + e.getMessage());
                } catch (SQLException e) {
                    // do nothing if it throws column not exists exception.
                    // continue to set other field.
                    logger.warn("setModelField-SQL: " + e.getMessage());
                }
            }
        } catch (IllegalAccessException | InstantiationException e) {
            logger.warn(e.getMessage());
            return null;
        }
        return model;
    }
    
    /**
     * Base jdbc insert operation. 
     * <p>Description: Only fields not null and annotated by Column and not by AutoIncrement. will be inserted. </p>
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: Jawinton </p>
     * @param model
     * @param tableName
     * @return newly inserted auto increment key.
     */
    public int insert(T model, String tableName) {
        return insert(model, tableName, false);
    }

    /**
     * If argument isIncludeAutoIncrementField is true, fields annotated by AutoIncrement will also be inserted.
     * <p>Description:              </p>
     * <p>Create Time: 2015年12月17日   </p>
     * <p>Create author: Jawinton   </p>
     * @param model
     * @param tableName
     * @param isIncludeAutoIncrementField
     * @return newly inserted auto increment key.
     */
    public int insert(T model, String tableName, boolean isIncludeAutoIncrementField) {
        StringBuilder sqlStringBuilder = new StringBuilder("INSERT INTO " + tableName + "");
        List<Object> paramsList = new ArrayList<Object>();
        try {
            StringBuilder keyStringBuilder = new StringBuilder(" (");
            StringBuilder valueStringBuilder = new StringBuilder(" (");
            for (Field field : getDeclaredFieldsIncludeSuperClasses(model.getClass())) {
                field.setAccessible(true);
                
                Object fieldValue = field.get(model);
                if (fieldValue == null)
                    continue;
                if (fieldValue instanceof Collection<?> || fieldValue instanceof Array)
                    fieldValue = JSONArray.toJSONString(fieldValue);
                else if (fieldValue instanceof Enum)
                    fieldValue = fieldValue.toString();
                
                if (!field.isAnnotationPresent(Column.class))
                    continue;
                
                if (isIncludeAutoIncrementField == false &&
                    field.isAnnotationPresent(AutoIncrement.class))
                    continue;
                
                Column annotation = field.getAnnotation(Column.class);
                String columnName = annotation.name();
                keyStringBuilder.append(" `" + columnName + "`,");
                valueStringBuilder.append(" ?,");
                paramsList.add(fieldValue);
            }
            keyStringBuilder.setCharAt(keyStringBuilder.length() - 1, ')');
            valueStringBuilder.setCharAt(valueStringBuilder.length() - 1, ')');
            sqlStringBuilder.append(keyStringBuilder.toString());
            sqlStringBuilder.append(" VALUES ");
            sqlStringBuilder.append(valueStringBuilder.toString());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.warn("BaseDao.insert-IllegalArgument： " + e.getMessage());
        }
        sqlStringBuilder.append(";");
//        return jdbcTemplate.update(sqlStringBuilder.toString());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sql =  sqlStringBuilder.toString();
        final Object[] params = paramsList.toArray();
        
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection con) throws SQLException {
                PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i+1, params[i]);
                }
                return preparedStatement;
            }
        }, keyHolder);
        // Get newly inserted id.
        Number key = keyHolder.getKey();
        // If auto generated key is null, return 1.
        return key == null ? 1 : key.intValue();
    }
    
    /**
     * Batch insert operation.	
     * <p>Description: Fields (except for fields annotated with PrimaryKey) will be updated if duplicated.
     * Null value field will also be updated, to be tests.
     * </p>
     * <p>Create Time: 2015年10月29日   </p>
     * <p>Create author: Jawinton   </p>
     * @param models
     * @param tableName
     * @return an array of the number of rows affected by each statement
     */
    public int[] batchInsert(final Collection<T> models, String tableName) {
        if (models == null || models.isEmpty())
            return null;
        
        StringBuilder sqlStringBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder insertStringBuilder = new StringBuilder();
        StringBuilder updateStringBuilder = new StringBuilder();
        final List<Field> insertFields = new ArrayList<>();
        final List<Field> updateFields = new ArrayList<>();
        final Object[] modelArray = models.toArray();
        Class<?> clazz = modelArray[0].getClass();
        String firstPrimaryKeyColumnName = null;
        for (Field field : getDeclaredFieldsIncludeSuperClasses(clazz)) {
            if (!field.isAnnotationPresent(Column.class))
                continue;
            if (field.isAnnotationPresent(AutoIncrement.class))
                continue;
            
            Column annotation = field.getAnnotation(Column.class);
            String columnName = annotation.name();
            sqlStringBuilder.append(" `" + columnName + "`,");
            insertStringBuilder.append(" ?,");
            insertFields.add(field);
            
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                if (firstPrimaryKeyColumnName == null) firstPrimaryKeyColumnName = columnName;
                continue;
            }
            
            updateStringBuilder.append(" `" + columnName + "` = ?,");
            updateFields.add(field);
        }
        insertStringBuilder.setCharAt(insertStringBuilder.length() - 1, ')');
        sqlStringBuilder.setCharAt(sqlStringBuilder.length() - 1, ')');
        sqlStringBuilder.append(" VALUES (");
        sqlStringBuilder.append(insertStringBuilder);
        insertStringBuilder = null;
        
        sqlStringBuilder.append(" ON DUPLICATE KEY UPDATE ");
        if (updateStringBuilder.length() > 0) {
            updateStringBuilder.setCharAt(updateStringBuilder.length() - 1, ';');
            sqlStringBuilder.append(updateStringBuilder);
            updateStringBuilder = null;
        } else {
            sqlStringBuilder.append("`" + firstPrimaryKeyColumnName + "` = VALUES(`" + firstPrimaryKeyColumnName + "`);" );
        }
        
        return jdbcTemplate.batchUpdate(sqlStringBuilder.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object model = modelArray[i];
                int parameterIndex = 1;
                for (Field field : insertFields) {
                    field.setAccessible(true);
                    Object fieldValue;
                    try {
                        fieldValue = field.get(model);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        fieldValue = null;
                    }
                    if (fieldValue instanceof Array || fieldValue instanceof Collection) {
                        fieldValue = JSONArray.toJSONString(fieldValue);
                    } else if (fieldValue instanceof Enum) {
                        fieldValue = fieldValue.toString();
                    }
                    ps.setObject(parameterIndex++, fieldValue);
                }
                for (Field field : updateFields) {
                    field.setAccessible(true);
                    Object fieldValue;
                    try {
                        fieldValue = field.get(model);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        fieldValue = null;
                    }
                    if (fieldValue instanceof Array || fieldValue instanceof Collection) {
                        fieldValue = JSONArray.toJSONString(fieldValue);
                    } else if (fieldValue instanceof Enum) {
                        fieldValue = fieldValue.toString();
                    }
                    ps.setObject(parameterIndex++, fieldValue);
                }
            }
            
            @Override
            public int getBatchSize() {
                return models.size();
            }
        });
    }
    
    /**
     * Update specified fields.	
     * <p>Description: If specified fields is null, update all fields
     * which are not null, annotated by Column and not by Transient. </p>
     * <p>Create Time: 2015年9月2日   </p>
     * <p>Create author: Jawinton </p>
     * @param condition
     * @param params
     * @param model
     * @param tableName
     * @param updateFields
     * @return affected rows.
     */
    public int updateFields(String condition, Object[] params, T model, String tableName, Collection<Field> updateFields) {
        SqlAndParam sqlAndParam = generateUpdateSqlAndParams(condition, params, model, tableName, updateFields);
        if (sqlAndParam == null)
            return 0;
        return jdbcTemplate.update(sqlAndParam.sql, sqlAndParam.params);
    }
    
    /**
     * Common jdbc update operation. 
     * <p>Description: Update all fields which are not null and annotated with Column. </p>
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: Jawinton </p>
     * @param condition
     * @param params
     * @param model
     * @param tableName
     * @return affected rows.
     */
    public int update(String condition, Object[] params, T model, String tableName) {
        return updateFields(condition, params, model, tableName, null);
    }
    
    /**
     * Batch update model list.	
     * <p>Description:              </p>
     * <p>Create Time: 2015年12月15日   </p>
     * <p>Create author: Jawinton   </p>
     * @param modelList
     * @param tableName
     * @return
     */
    public int[] batchUpdate(Collection<T> modelList, String tableName) {
        if (modelList == null)
            return null;
        List<Object[]> batchArgs = new ArrayList<>();
        String sql = null;
        for (T model : modelList) {
            SqlAndParam conditionSqlAndParam = generateUpdateConditionSqlAndParam(model);
            if (conditionSqlAndParam == null)
                return null;
            SqlAndParam sqlAndParam = generateUpdateSqlAndParams(conditionSqlAndParam.sql, conditionSqlAndParam.params, model, tableName, null);
            if (sqlAndParam == null)
                return null;
            batchArgs.add(sqlAndParam.params);
            if (sql == null) {
                sql = sqlAndParam.sql;
            } else if (!sql.equals(sqlAndParam.sql)) { // every sql must be same to each other.
                return null;
            }
        }
        if (sql == null)
            return null;
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }
    
    /**
     * Update model by primary key.	
     * <p>Description: Multiple primary keys are supported.  </p>
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: Jawinton   </p>
     * @param model
     * @param tableName
     * @return
     */
    public int update(T model, String tableName) {
        SqlAndParam conditionSqlAndParam = generateUpdateConditionSqlAndParam(model);
        if (conditionSqlAndParam == null)
            return 0;
        return updateFields(conditionSqlAndParam.sql, conditionSqlAndParam.params, model, tableName, null);
    }
    
    /**
     * Delete model by primary key.	
     * <p>Description:              </p>
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: Jawinton   </p>
     * @param primaryKeyValue
     * @param clazz
     * @param tableName
     * @return
     */
    public int delete(Object primaryKeyValue, Class<T> clazz, String tableName) {
        for (Field field : getDeclaredFieldsIncludeSuperClasses(clazz)) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(PrimaryKey.class))
                continue;
            if (!field.isAnnotationPresent(Column.class))
                continue;
            StringBuilder condition = new StringBuilder();
            Column annotation = field.getAnnotation(Column.class);
            String columnName = annotation.name();
            condition.append(" AND `" + columnName + "` = ?");
            return delete(condition.toString(), new Object[]{primaryKeyValue}, tableName);
        }
        return 0;
    }
    
    /**
     * Base jdbc query operation.
     * <p>Description: Get all model fields. </p>
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: Jawinton   </p>
     * @param condition
     * @param params
     * @param clazz
     * @param tableName
     * @return
     */
    public List<T> query(String condition, Object[] params, Class<T> clazz, String tableName) {
        return query(condition, params, null, clazz, tableName);
    }
    
    /**
     *  Base jdbc query operation.
     * <p>Description: filter model fields by fields. </p>
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: Jawinton </p>
     * @param condition
     * @param params
     * @param fields
     * @param tableName
     * @return
     */
    public List<T> query(String condition, Object[] params, Field[] fields, Class<T> clazz, String tableName) {
        StringBuilder fieldStringBuilder = new StringBuilder();
        if (fields == null) {
            fieldStringBuilder.append(" *");
        } else {
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Column.class))
                    continue;
                Column annotation = field.getAnnotation(Column.class);
                String columnName = annotation.name();
                fieldStringBuilder.append(" `" + columnName + "`,");
            }
            fieldStringBuilder.setCharAt(fieldStringBuilder.length() - 1, ' ');
        }
        final String sql = "SELECT "
                + fieldStringBuilder.toString()
                + " FROM " + tableName
                + ((condition == null || condition.trim().equals("")) ? "" : (" WHERE 1 = 1 " + condition));
        return jdbcTemplate.query(sql, params, new BaseMapper(clazz));
    }
    
    /**
     * Query sql directly.	
     * <p>Description:              </p>
     * <p>Create Time: 2015年11月17日   </p>
     * <p>Create author: Jawinton   </p>
     * @param sql
     * @param clazz
     * @param params
     * @return
     */
    public List<T> query(String sql, Class<T> clazz, Object... params) {
        return jdbcTemplate.query(sql, params, new BaseMapper(clazz));
    }
    
    /**
     * Get one model by condition.
     * <p>Description:              </p>
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: Jawinton   </p>
     * @param condition
     * @param params
     * @param fields
     * @param clazz
     * @param tableName
     * @return
     */
    public T queryOne(String condition, Object[] params, Field[] fields, Class<T> clazz, String tableName) {
        List<T> models = query(condition, params, fields, clazz, tableName);
        if (models == null || models.isEmpty())
            return null;
        return models.get(0);
    }
    
    /**
     * Get one model by primary key.
     * <p>Description: Multiple primary keys are supported. </p>
     * <p>Create Time: 2015年11月11日   </p>
     * <p>Create author: Jawinton   </p>
     * @param model
     * @param clazz
     * @param tableName
     * @return
     */
    public T queryOne(Class<T> clazz, String tableName, T model) {
        if (model == null)
            return null;
        StringBuilder condition = new StringBuilder();
        List<Object> primaryKeyValues = new ArrayList<>();
        for (Field field : getDeclaredFieldsIncludeSuperClasses(clazz)) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(PrimaryKey.class))
                continue;
            if (!field.isAnnotationPresent(Column.class))
                continue;
            Column annotation = field.getAnnotation(Column.class);
            String columnName = annotation.name();
            condition.append(" AND `" + columnName + "` = ?");
            try {
                primaryKeyValues.add(field.get(model));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // if exception, number of primary key values will not meet number of primary key annotation.
                return null;
            }
        }
        return queryOne(condition.toString(), primaryKeyValues.toArray(), null, clazz, tableName);
    }
    
    private class BaseMapper implements RowMapper<T> {

        private Class<T> clazz;
        
        public BaseMapper(Class<T> clazz) {
            this.clazz = clazz;
        }
        
        @Override
        public T mapRow(ResultSet set, int arg1) throws SQLException {
            return setModelField(set, this.clazz);
        }
    }
    
    /**
     * 	
     * <p>Description:  Base get total count operation. </p>
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: Jawinton  </p>
     * @param condition
     * @param params
     * @param tableName
     * @return
     */
    public int getCount(String condition, Object[] params, String tableName) {
        final String sql = "SELECT COUNT(1) "
                + "FROM " + tableName
                + ((condition == null || condition.trim().equals("")) ? "" : (" WHERE 1 = 1 " + condition));
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    /**
     * 	
     * <p>Description: Base jdbc delete operation. </p>
     * <p>Create Time: 2015年7月14日   </p>
     * <p>Create author: Jawinton </p>
     * @param condition
     * @param params
     * @param tableName
     * @return
     */
    public int delete(String condition, Object[] params, String tableName) {
        if (condition == null || "".equals(condition.trim()))
                return 0;
        String sql="DELETE FROM " + tableName + " WHERE 1 = 1 " + condition;
        return jdbcTemplate.update(sql,params);
    }
    
    private Collection<Field> getDeclaredFieldsIncludeSuperClasses(Class<?> clazz) {
        Collection<Field> fields = new ArrayList<>();
        for(; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // class.getDeclaredFields() will not return null.
            fields.addAll(ImmutableList.<Field>copyOf(clazz.getDeclaredFields()));
        }
        return fields;
    }
    
    private class SqlAndParam {
        private String sql;
        private Object[] params;
        
        private SqlAndParam(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
    }
    
    private SqlAndParam generateUpdateSqlAndParams(String condition, Object[] params, T model, String tableName, Collection<Field> updateFields) {
        StringBuilder sqlStringBuilder = new StringBuilder("UPDATE " + tableName + " SET ");
        StringBuilder keyStringBuilder = new StringBuilder();
        List<Object> paramsList = new ArrayList<Object>();
        try {
            for (Field field : getDeclaredFieldsIncludeSuperClasses(model.getClass())) {
                field.setAccessible(true);
                Object fieldValue;
                if (!field.getType().isArray() && field.getType() != Collection.class)
                    fieldValue = field.get(model);
                else
                    fieldValue = JSONArray.toJSONString(field.get(model));
                
                if (fieldValue == null)
                    continue;
                if (!field.isAnnotationPresent(Column.class))
                    continue;
                if (field.isAnnotationPresent(PrimaryKey.class))
                    continue;
                
                // only update field in specified fields collection.
                if (updateFields == null || updateFields.contains(field)) {
                    Column annotation = field.getAnnotation(Column.class);
                    String columnName = annotation.name();
                    keyStringBuilder.append(" `" + columnName + "` = ?,");
                    paramsList.add(fieldValue instanceof Enum ? fieldValue.toString() : fieldValue);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.warn("generateUpdateSqlAndParams-IllegalArgument: " + e.getMessage());
        }
        // no field to update.
        if (keyStringBuilder.length() == 0)
            return null;
        keyStringBuilder.setCharAt(keyStringBuilder.length() - 1, ' ');
        sqlStringBuilder.append(keyStringBuilder.toString());
        Object[] updateParams = paramsList.toArray();
        Object[] sqlParams = new Object[updateParams.length + params.length];
        System.arraycopy(updateParams, 0, sqlParams, 0, updateParams.length);
        System.arraycopy(params, 0, sqlParams, updateParams.length, params.length);
        sqlStringBuilder.append(((condition == null || condition.trim().equals("")) ? "" : (" WHERE 1 = 1 " + condition)));
        sqlStringBuilder.append(";");
        return new SqlAndParam(sqlStringBuilder.toString(), sqlParams);
    }
    
    private SqlAndParam generateUpdateConditionSqlAndParam(T model) {
        if (model == null)
            return null;
        StringBuilder condition = new StringBuilder();
        List<Object> primaryKeyValues = new ArrayList<>();
        for (Field field : getDeclaredFieldsIncludeSuperClasses(model.getClass())) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(PrimaryKey.class))
                continue;
            if (!field.isAnnotationPresent(Column.class))
                continue;
            Column annotation = field.getAnnotation(Column.class);
            String columnName = annotation.name();
            condition.append(" AND `" + columnName + "` = ?");
            try {
                primaryKeyValues.add(field.get(model));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // if exception, number of primary key values will not meet number of primary key annotation.
                return null;
            }
        }
        return new SqlAndParam(condition.toString(), primaryKeyValues.toArray());
    }
}
