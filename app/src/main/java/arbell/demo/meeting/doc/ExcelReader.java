package arbell.demo.meeting.doc;

import android.util.Log;

import com.artifex.mupdfdemo.AsyncTask;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 2015-09-22 08:45
 */
public class ExcelReader extends AsyncTask<File, Void, Workbook> {
    @Override
    protected Workbook doInBackground(File... params) {
        File file = params[0];
        String suffix = GetDocUrl.getSuffix(file.getName());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            if("xls".equals(suffix))
                return new HSSFWorkbook(fis);
            else if("xlsx".equals(suffix))
                return new XSSFWorkbook(fis);
            else
                return null;
        } catch (IOException e) {
            Log.e("Meeting", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            return null;
        }
        finally {
            if(fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    //ignore
                }
        }
    }
}
