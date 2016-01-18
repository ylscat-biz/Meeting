package arbell.demo.meeting.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * 2015-09-18 23:26
 */
public class ExcelView extends View {
    private int mOffsetX, mOffsetY;

    private SheetData mData;
    private Paint mPaint;
    private float mTextSize;
    private float mScale = 1;

    public ExcelView(Context context) {
        this(context, null);
    }

    public ExcelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.LTGRAY);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22,
                getResources().getDisplayMetrics()));
        mTextSize = mPaint.getTextSize();
    }

    public void setData(SheetData data) {
        mData = data;
        mOffsetY = 0;
        mOffsetX = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mData == null)
            return;

        float h = getHeight();
        float w = getWidth();
        float scale = mScale;
        float gap = mData.mGap*scale;

        int firstRow, lastRow;
        int firstCol, lastCol;

        ArrayList<Integer> posX = mData.mPosX;
        ArrayList<Integer> posY = mData.mPosY;
        if(posX.size() == 0 || posY.size() == 0)
            return;

        firstCol = Math.max(0, findProperIndex(posX, mOffsetX/scale));
        lastCol = Math.min(posX.size() - 1, findProperIndex(posX, (mOffsetX + w)/scale));
        firstRow = Math.max(0, findProperIndex(posY, mOffsetY/scale));
        lastRow = Math.min(posY.size() - 1, findProperIndex(posY, (mOffsetY + h)/scale));


        HashMap<Integer, CellData> data = mData.mCellData;
        ArrayList<Integer> rowHeights = mData.mRowHeights;
        ArrayList<Integer> colWidths = mData.mColWidths;
        Paint paint = mPaint;

        float y = (posY.get(firstRow) - rowHeights.get(firstRow))*scale - mOffsetY;
        float initX = (posX.get(firstCol) - colWidths.get(firstCol))*scale - mOffsetX;
        for (int r = firstRow; r <= lastRow; r++) {
            float x = initX;
            h = (int)(rowHeights.get(r)*scale);

            for(int c = firstCol; c <= lastCol; c++) {
                int index = r*SheetData.X_MUL + c;
                CellData cellData = data.get(index);
                w = colWidths.get(c)*scale;

                paint.setColor(Color.WHITE);
                if(cellData == null) {
                    canvas.drawRect(x, y, x + w, y + h, paint);
                }
                else if(cellData.spanX >= 0 && cellData.spanY >= 0){
                    float fixW = w, fixH = h;
                    for(int v = 1; v <= cellData.spanX; v++) {
                        fixW += colWidths.get(c + v)*scale + gap;
                    }
                    for(int v = 1; v <= cellData.spanY; v++) {
                        fixH += rowHeights.get(r + v)*scale + gap;
                    }

                    float right = x + fixW;
                    float bottom = y + fixH;

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
                } else
borderMerge:    {
                    if(r == firstRow && cellData.spanY < 0) {
                        int sRow = r + cellData.spanY;
                        int sCol = c + cellData.spanX;
                        CellData src = data.get(sRow*SheetData.X_MUL + sCol);
                        //只在最后一列，绘制一次
                        if(src.spanX > -cellData.spanX)
                            break borderMerge;

                        float right = x + w;
                        float bottom = y + h;
                        for(int i = -cellData.spanY + 1; i <= src.spanY; i++) {
                            bottom += rowHeights.get(sRow + i)*scale + gap;
                        }

                        float left = x;
                        for(int i = c - 1; i >= sCol; i--) {
                            left -= colWidths.get(i)*scale + gap;
                        }
                        float top = y;
                        for(int i = r - 1; i >= sRow; i--) {
                            top -= rowHeights.get(i)*scale + gap;
                        }

                        canvas.drawRect(left, top, right, bottom, paint);
                        String str = src.value;
                        if(str != null) {
                            paint.setColor(Color.BLACK);

                            canvas.save();
                            canvas.clipRect(left, top, right, bottom);
                            canvas.translate(left, top);
                            drawText(str, canvas, paint, right - left, bottom - top);

                            canvas.restore();
                        }
                    }
                    else if(c == firstCol && cellData.spanX < 0) {
                        int sRow = r + cellData.spanY;
                        int sCol = c + cellData.spanX;
                        CellData src = data.get(sRow*SheetData.X_MUL + sCol);
                        //只在最后一行，绘制一次
                        if(src.spanY > -cellData.spanY)
                            break borderMerge;
                        //如果同时单元格合并了多列，那么已经被处理了，不用再画了
//                        if(src.spanX > 0)
//                            continue;

                        float bottom = y + h;
                        float right = x + w;
                        for(int i = -cellData.spanX + 1; i <= src.spanX; i++) {
                            right += colWidths.get(sCol + i)*scale + gap;
                        }

                        float left = x;
                        for(int i = c - 1; i >= sCol; i--) {
                            left -= colWidths.get(i)*scale + gap;
                        }
                        float top = y;
                        for(int i = r - 1; i >= sRow; i--) {
                            top -= rowHeights.get(i)*scale + gap;
                        }

                        canvas.drawRect(left, top, right, bottom, paint);
                        String str = src.value;
                        if(str != null) {
                            paint.setColor(Color.BLACK);

                            canvas.save();
                            canvas.clipRect(left, top, right, bottom);
                            canvas.translate(left, top);
                            drawText(str, canvas, paint, right - left, bottom - top);

                            canvas.restore();
                        }
                    }
                }
                x += w + gap;
            }
            y += h + gap;
        }
    }

    private int findProperIndex(ArrayList<Integer> list, Number value) {
        int index = Collections.binarySearch(list, value, NUM_COMPARATOR);
        if(index >= 0)
            return index;
        else {
            return -index - 1;
        }
    }

    private void drawText(String str, Canvas canvas, Paint paint, float w, float h) {
        float size = mTextSize*mScale;
        paint.setTextSize(size);
        int len = paint.breakText(str, 0, str.length(), true, w, null);
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
//            if(++line == maxLines)
//                len += 1;
            canvas.drawText(str, start, start + len, 0, ty, paint);
            start += len;
            if(start == end)
                break;
            len = paint.breakText(str, start, end, true, w, null);
            ty += size;
            line++;
        }
    }

    public void scrollBy(int dx, int dy) {
        if(mData == null)
            return;
        int x = mOffsetX + dx;
        int y = mOffsetY + dy;
        scrollTo(x, y);
    }

    public void scrollTo(int x, int y) {
        float scale = mScale;

        if(x < 0) {
            x = 0;
        }
        else {
            ArrayList<Integer> posX = mData.mPosX;
            int last = posX.size() - 1;
            if(last == -1)
                return;
            int maxX = Math.max(0, (int)(posX.get(last)*scale - getWidth()));
            if(x > maxX)
                x = maxX;
        }

        if(y < 0) {
            y = 0;
        }
        else {
            ArrayList<Integer> posY = mData.mPosY;
            int last = posY.size() - 1;
            if(last == -1)
                return;
            int maxY = Math.max(0, (int)(posY.get(last)*scale - getHeight()));
            if(y > maxY)
                y = maxY;
        }

        if(x == mOffsetX && y == mOffsetY)
            return;
        mOffsetX = x;
        mOffsetY = y;

        invalidate();
    }

    public void scaleBy(float delta, float px, float py) {
        float scale = delta*mScale;
        if(scale < 0.1f)
            scale = .1f;
        else if(scale > 3)
            scale = 3;
        if(mScale == scale)
            return;
        mScale = scale;

//        float dx = (scale - preScale)*( + mOffsetX);
//        float dy = (scale - preScale)*( + mOffsetY);
        float x = mOffsetX*delta + px*(delta - 1);
        float y = mOffsetY*delta + py*(delta - 1);

        scrollTo((int) x, (int) y);
    }

    public int getMaxOffsetX() {
        ArrayList<Integer> posX = mData.mPosX;
        int last = posX.size() - 1;
        if(last == -1)
            return 0;
        return Math.max(0, (int)(posX.get(last)*mScale - getWidth()));
    }

    public int getMaxOffsetY() {
        ArrayList<Integer> posY = mData.mPosY;
        int last = posY.size() - 1;
        if(last == -1)
            return 0;
        return Math.max(0, (int)(posY.get(last)*mScale - getHeight()));
    }

    public static class CellData {
        public String value;
        public int spanX, spanY;
    }

    public static class SheetData {
        public static final int X_MUL = 10000;
        public ArrayList<Integer> mRowHeights = new ArrayList<>();
        public ArrayList<Integer> mColWidths = new ArrayList<>();
        public ArrayList<Integer> mPosX = new ArrayList<>();
        public ArrayList<Integer> mPosY = new ArrayList<>();
        public HashMap<Integer, CellData> mCellData = new HashMap<>();
        public int mGap;

        public SheetData (Sheet sheet, float density) {
            ArrayList<Integer> heights = mRowHeights;
            ArrayList<Integer> widths = mColWidths;
            ArrayList<Integer> posX = mPosX;
            ArrayList<Integer> posY = mPosY;
            HashMap<Integer, CellData> cells = mCellData;
            mGap = Math.max((int)density, 1);

            for(int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress range = sheet.getMergedRegion(i);
                int firstColumn = range.getFirstColumn();
                int firstRow = range.getFirstRow();
                Cell cell = sheet.getRow(firstRow).getCell(firstColumn);
                CellData data = new CellData();
                data.spanX = range.getLastColumn() - firstColumn;
                data.spanY = range.getLastRow() - firstRow;
                data.value = getCellValue(cell);
                cells.put((firstRow * X_MUL + firstColumn), data);
                int lastCol = range.getLastColumn();
                for(int k = 1 + firstColumn; k <= lastCol; k++) {
                    data = new CellData();
                    data.spanX = firstColumn - k;
                    cells.put((firstRow*X_MUL + k), data);
                }

                for(int k = 1 + firstRow; k <= range.getLastRow(); k++) {
                    for(int j = firstColumn; j <= lastCol; j++) {
                        data = new CellData();
                        data.spanX = firstColumn - j;
                        data.spanY = firstRow - k;
                        cells.put((k*X_MUL + j), data);
                    }
                }
            }

            int colCount = 0;
            int y = 0, x = 0;
            int gap = mGap;
            for(int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(row == null) {
                    int h = (int)(sheet.getDefaultRowHeight()*density/8);
                    heights.add(h);
                    y += h + gap;
                    posY.add(y);
                    continue;
                }
                int h = ((int)(row.getHeight()*density/8));
                heights.add(h);
                y += h + gap;
                posY.add(y);
                int cc = row.getLastCellNum();

                for(int j = row.getFirstCellNum(); j <= cc; j++) {
                    if(colCount <= cc) {
                        int w = (int)(sheet.getColumnWidth(colCount)*density/20);
                        widths.add(w);
                        x += w + gap;
                        posX.add(x);
                        colCount++;
                    }

                    int index = X_MUL*i + j;
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
            GestureDetector.OnGestureListener,
            ScaleGestureDetector.OnScaleGestureListener,
            Runnable {
        private ExcelView mView;
        private GestureDetector mDetector;
        private ScaleGestureDetector mScaleDetector;
        private float mInitialSpan = 0;
        private Scroller mScroller;

        public GestureController(ExcelView view) {
            mView = view;
            Context context = view.getContext();
            mDetector = new GestureDetector(context, this);
            mScaleDetector = new ScaleGestureDetector(context, this);
            mScroller = new Scroller(context);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if(!mScroller.isFinished()){
                mScroller.abortAnimation();
            }
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
            if(mInitialSpan == 0)
                mView.scrollBy((int)distanceX, (int)distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mScroller.fling(mView.mOffsetX, mView.mOffsetY, -(int)velocityX, -(int)velocityY,
                    0, mView.getMaxOffsetX(), 0, mView.getMaxOffsetY());
            run();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            ExcelView view = mView;
            float scale = detector.getScaleFactor();
            view.scaleBy(scale, detector.getFocusX(), detector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mInitialSpan = detector.getCurrentSpan();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mInitialSpan = 0;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mScaleDetector.onTouchEvent(event);
            mDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public void run() {
            if(mScroller.computeScrollOffset()) {
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();
                mView.mOffsetX = x;
                mView.mOffsetY = y;
                mView.invalidate();
                mView.post(this);
            }
        }
    }

    private static final Comparator<Number> NUM_COMPARATOR = new Comparator<Number>() {
        @Override
        public int compare(Number lhs, Number rhs) {
            return (int)((lhs.doubleValue() - rhs.doubleValue())*10000);
        }
    };
}
