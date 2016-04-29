package nd.esp.service.lifecycle.support.excel;

import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.poi.ss.usermodel.Row;
import org.junit.Assert;

/**
 * @title pio帮助类
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月29日 上午10:14:15
 */
public class PioHelper {
	
	
	/**
	 * @desc 判断第row行,第index(0是第一行)列是否为空
	 * @param row
	 * @param index
	 * @return
	 * @author liuwx
	 */
	public  static boolean isCellEmpty(Row row,int index){
		Assert.assertNotNull("row 不能为空", row);
		Assert.assertTrue("index不能小于0", index<0);
		Assert.assertNotNull("第"+index+"个cell不存在", row.getCell(index));
		return StringUtils.hasText(row.getCell(index).getStringCellValue());
	}

}
