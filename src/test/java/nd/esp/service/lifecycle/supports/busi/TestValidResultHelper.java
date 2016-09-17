package nd.esp.service.lifecycle.supports.busi;


import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.support.busi.ValidResultHelper;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class TestValidResultHelper {
	
	public static void main(String[] args) {
		
		BindingResult validResult=new BindingResult() {
			
			@Override
			public void setNestedPath(String nestedPath) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void rejectValue(String field, String errorCode, Object[] errorArgs,
					String defaultMessage) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void rejectValue(String field, String errorCode,
					String defaultMessage) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void rejectValue(String field, String errorCode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reject(String errorCode, Object[] errorArgs,
					String defaultMessage) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reject(String errorCode, String defaultMessage) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reject(String errorCode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void pushNestedPath(String subPath) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void popNestedPath() throws IllegalStateException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean hasGlobalErrors() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasFieldErrors(String field) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasFieldErrors() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasErrors() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public String getObjectName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getNestedPath() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<ObjectError> getGlobalErrors() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getGlobalErrorCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public ObjectError getGlobalError() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getFieldValue(String field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Class<?> getFieldType(String field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<FieldError> getFieldErrors(String field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<FieldError> getFieldErrors() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getFieldErrorCount(String field) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getFieldErrorCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public FieldError getFieldError(String field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public FieldError getFieldError() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getErrorCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public List<ObjectError> getAllErrors() {
				List<ObjectError> errors=new ArrayList<ObjectError>();
				ObjectError error=new ObjectError("param1","param2");
				errors.add(error);
				return errors;
			}
			
			@Override
			public void addAllErrors(Errors errors) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String[] resolveMessageCodes(String errorCode, String field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String[] resolveMessageCodes(String errorCode) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void recordSuppressedField(String field) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Object getTarget() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String[] getSuppressedFields() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getRawFieldValue(String field) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public PropertyEditorRegistry getPropertyEditorRegistry() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String, Object> getModel() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public PropertyEditor findEditor(String field, Class<?> valueType) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void addError(ObjectError error) {
				// TODO Auto-generated method stub
				
			}
		};
		//ValidResultHelper.valid(validResult);
		ValidResultHelper.valid(validResult,"my code");
		
	;
	}

}
