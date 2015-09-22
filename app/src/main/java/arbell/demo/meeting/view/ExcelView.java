package arbell.demo.meeting.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 2015-09-18 23:26
 */
public class ExcelView extends View {
    private int mFirstRow, mFirstColumn;
    private int mOffsetX, mOffsetY;
    private int mGap;

    private SheetData mData;
    private Paint mPaint;
    private float mTextSize;

    public ExcelView(Context context) {
        this(context, null);
    }

    public ExcelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.LTGRAY);

        mGap = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                getResources().getDisplayMetrics());
        mGap = Math.max(1, mGap);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22,
                getResources().getDisplayMetrics()));
        mTextSize = mPaint.getTextSize();
    }

    public void setData(SheetData data) {
        mData = data;
        mFirstRow = 0;
        mFirstColumn = 0;
        mOffsetY = 0;
        mOffsetX = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mData == null)
            return;

        int h = getHeight();
        int w = getWidth();
        int gap = mGap;

        int bottom = mOffsetY;
        int lastRow = mFirstRow;

        ArrayList<Integer> rowHeights = mData.mRowHeights;
        ArrayList<Integer> colWidths = mData.mColWidths;
        if(colWidths.size() == 0 || rowHeights.size() == 0)
            return;

        while (bottom < h) {
            if(lastRow == rowHeights.size() - 1)
                break;
            bottom += rowHeights.get(++lastRow) + gap;
        }
//        for(;lastRow < rowHeights.size(); lastRow++) {
//            bottom += rowHeights.get(lastRow) + gap;
//            if(bottom > h)
//                break;
//        }

        int right = mOffsetX;
        int lastCol = mFirstColumn;
        while (right < w) {
            if(lastCol == colWidths.size() - 1)
                break;
            right += colWidths.get(++lastCol) + gap;
        }
//        for(;lastCol < colWidths.size(); lastCol++) {
//            right += colWidths.get(lastCol) + gap;
//            if(right > w)
//                break;
//        }


        HashMap<Integer, CellData> data = mData.mCellData;
        Paint paint = mPaint;

        int y = mOffsetY;

        for (int r = mFirstRow; r <= lastRow; r++) {
            int x = mOffsetX;
            h = rowHeights.get(r);

            for(int c = mFirstColumn; c <= lastCol; c++) {
                int index = r*1000 + c;
                CellData cellData = data.get(index);
                w = colWidths.get(c);

                paint.setColor(Color.WHITE);
                if(cellData == null) {
                    canvas.drawRect(x, y, x + w, y + h, paint);
                }
                else if(cellData.spanX >= 0 && cellData.spanY >= 0){
                    int fixW = w, fixH = h;
                    for(int v = 1; v <= cellData.spanX; v++) {
                        fixW += colWidths.get(c + v) + gap;
                    }
                    for(int v = 1; v <= cellData.spanY; v++) {
                        fixH += rowHeights.get(r + v) + gap;
                    }

                    right = x + fixW;
                    bottom = y + fixH;

                    canvas.drawRect(x, y, right, bottom, paint);
                    String str = cellData.value;
                    if(str != null) {
                        paint.setColor(Color.BLACK);

                        canvas.save();
                        canvas.clipRect(x, y, right, bottom);
                        canvas.translate(x, y);
                        drawText(str, canvas, paint, fixW, fixH);
//                        int end = str.length();
//                        int ty = (int)-paint.ascent() + y;
//                        for(int start = 0; start < end;) {
//                            int len = paint.breakText(str, start, end, true, fixW, null);
//                            canvas.drawText(str, start, start + len, x, ty, paint);
//                            ty += mTextSize;
//                            start += len;
//                        }

                        canvas.restore();
                    }
                } /*else {
                    if(r == mFirstRow && cellData.spanY < 0) {
                        if(c == mFirstColumn || cellData.spanX >= -1) {

                        }
                    }
                }*/
                x += w + gap;
            }
            y += h + gap;
        }
    }

    private void drawText(String str, Canvas canvas, Paint paint, int w, int h) {

        int len = paint.breakText(str, 0, str.length(), true, w, null);
        float size = mTextSize;
        if(len == str.length()) {
            canvas.drawText(str, 0, (h - size)/2 -paint.ascent(), paint);
            return;
        }

        int maxLines = Math.round(h / size);
        if(maxLines == 0)
            maxLines = 1;

        if(maxLines == 1) {
            canvas.drawText(str, 0, len + 1, 0, (h - size)/2 -paint.ascent(), paint);
            return;
        }

        int start = 0;
        int end = str.length();
        float ty = -paint.ascent();
        for(int line = 0; line < maxLines; ) {
            if(++line == maxLines)
                len += 1;
            canvas.drawText(str, start, start + len, 0, ty, paint);
            start += len;
            if(start == end)
                break;
            len = paint.breakText(str, start, end, true, w, null);
            ty += size;
        }
    }

    public void scrollBy(int dx, int dy) {
        if(mData == null)
            return;
        int x = mOffsetX + dx;
        int y = mOffsetY + dy;

        ArrayList<Integer> heights = mData.mRowHeights;
        ArrayList<Integer> widths = mData.mColWidths;
        if(widths.size() == 0 || heights.size() == 0)
            return;
        if(x > 0) {
            int first = mFirstColumn;
            while (first > 0) {
                int w = widths.get(--first);
                x -= w;
                if(x < 0) {
                    mOffsetX = x;
                    break;
                }
            }
            mFirstColumn = first;
        }
        else if(-x > widths.get(mFirstColumn)) {
            int first = mFirstColumn;
            int w = widths.get(first);
            while (first + 1 < widths.size()) {
                x += w;
                w = widths.get(++first);
                if(-x < w)
                    break;
            }
            mFirstColumn = first;
            mOffsetX = x;
        }
        else
            mOffsetX = x;

        if(y > 0) {
            int first = mFirstRow;
            while (first > 0) {
                int h = heights.get(--first);
                y -= h;
                if(y < 0) {
                    mOffsetY = y;
                    break;
                }
            }
            mFirstRow = first;
        }
        else if(-y > heights.get(mFirstRow)) {
            int first = mFirstRow;
            int h = heights.get(first);
            while (first + 1 < heights.size()) {
                y += h;
                h = heights.get(++first);
                if(-y < h)
                    break;
            }
            mFirstRow = first;
            mOffsetY = y;
        }
        else
            mOffsetY = y;

        invalidate();
    }

    public static class CellData {
        public String value;
        public int spanX, spanY;
    }

    public static class SheetData {
        public ArrayList<Integer> mRowHeights = new ArrayList<>();
        public ArrayList<Integer> mColWidths = new ArrayList<>();
        public HashMap<Integer, CellData> mCellData = new HashMap<>();

        public SheetData (Sheet sheet, float density) {
            ArrayList<Integer> heights = mRowHeights;
            ArrayList<Integer> widths = mColWidths;
            HashMap<Integer, CellData> cells = mCellData;

            for(int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress range = sheet.getMergedRegion(i);
                int firstColumn = range.getFirstColumn();
                int firstRow = range.getFirstRow();
                Cell cell = sheet.getRow(firstRow).getCell(firstColumn);
                CellData data = new CellData();
                data.spanX = range.getLastColumn() - firstColumn;
                data.spanY = range.getLastRow() - firstRow;
                data.value = getCellValue(cell);
                cells.put((firstRow * 1000 + firstColumn), data);
                int lastCol = range.getLastColumn();
                for(int k = 1 + firstColumn; k <= lastCol; k++) {
                    data = new CellData();
                    data.spanX = firstColumn - k;
                    cells.put((firstRow*1000 + k), data);
                }

                for(int k = 1 + firstRow; k <= range.getLastRow(); k++) {
                    for(int j = firstColumn; j <= lastCol; j++) {
                        data = new CellData();
                        data.spanX = firstColumn - j;
                        data.spanY = firstRow - k;
                        cells.put((k*1000 + j), data);
                    }
                }
            }

            int colCount = 0;
            for(int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(row == null) {
                    heights.add((int)(sheet.getDefaultRowHeight()*density/8));
                    continue;
                }
                heights.add((int)(row.getHeight()*density/8));
                int cc = row.getLastCellNum();

                for(int j = row.getFirstCellNum(); j <= cc; j++) {
                    if(colCount <= cc) {
                        widths.add((int)(sheet.getColumnWidth(colCount)*density/20));
                        colCount++;
                    }

                    int index = 1000*i + j;
                    if(cells.containsKey(index))
                        continue;
                    Cell cell = row.getCell(j);
                    if(cell == null)
                        continue;
                    String value = getCellValue(cell);
                    if(value != null) {
                        CellData data = new CellData();
                        data.value = value;
                        cells.put(index, data);
                    }
                }
            }
        }

        private String getCellValue(Cell cell) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_BLANK:
                    return null;
                case Cell.CELL_TYPE_NUMERIC:
                    double v = cell.getNumericCellValue();
                    long l = (long)v;
                    if(l == v)
                        return String.valueOf(l);
                    else
                        return Double.toString(v);
                case Cell.CELL_TYPE_BOOLEAN:
                    return Boolean.toString(cell.getBooleanCellValue());
                case Cell.CELL_TYPE_FORMULA:
                    return cell.getCellFormula();
                default:
                    return null;
            }
        }
    }

    public static class GestureController implements View.OnTouchListener,
            GestureDetector.OnGestureListener {
        private ExcelView mView;
        private GestureDetector mDetector;

        public GestureController(ExcelView view) {
            mView = view;
            mDetector = new GestureDetector(view.getContext(), this);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mView.scrollBy(-(int)distanceX, -(int)distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mDetector.onTouchEvent(event);
        }
    }
}
