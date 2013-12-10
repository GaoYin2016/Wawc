package com.wise.extend;

import java.math.BigDecimal;
import java.util.ArrayList;
import com.wise.data.EnergyItem;
import com.wise.wawc.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
/**
 * 油耗曲线
 * @author honesty
 */
public class EnergyCurveView extends View implements OnTouchListener {
	private final static String TAG = "testActivity";
	Context context;
	private static final float SPACING_SCALE = 40f; // 缩放间距 用于背景图过大
	/**
	 * 距离顶部的高度
	 */
	private static final float SPACING_HEIGHT = 40f;
	/**
	 * y轴最高刻度 距离 y轴绘制的 间距
	 */
	private static final float WEIGHT = 20f;
	/**
	 * 距离左边的间距
	 */
	private static final float SPACING = 35f;
	private Bitmap mDegree; // 度数显示框
	private Bitmap mTrendLine; // 点击显示的竖线
	private Bitmap mLastPoint; // 曲线图最后一个绘制灰色圆点
	private Bitmap mMovePoint; // 曲线图移动中绘制的圆点
	private float mGradientWidth; // 渐变条的宽度
	private float mGradientHeight = 450; // 渐变条的高度
	private DisplayMetrics dm; // 手机屏幕的宽高
	private ArrayList<PointF> points; // 有消耗的电量时间点
	private ArrayList<EnergyItem> energyItems;
	private float spacingOfX; // X间距
	private float spacingOfY; // Y间距,每一度的间距
	private EnergyItem maxEnergy; //y坐标最大的单元
	private float downYOfFirst; // 单个按下的Y的坐标
	private float moveXOfFirst; // 单个按下的X坐标
	private float moveYOfFirst; // 单个按下的Y的坐标
	private boolean isMove = false; // 可以移动

	public EnergyCurveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(context);
	}

	private void init(Context context) {
		setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch");
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			isMove = false;
			if(!isClick(event)){
				Log.d(TAG, "ACTION_DOWN");
				downYOfFirst = event.getY(0);
				moveXOfFirst = event.getX(0);
				moveYOfFirst = event.getY(0);
			}			
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			isMove = true;
			Log.d(TAG, "ACTION_MOVE");
			moveXOfFirst = event.getX(0);
			moveYOfFirst = event.getY(0);
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			Log.d(TAG, "ACTION_UP");
			if((!isMove)&&isClick(event)){				
				//Intent intent = new Intent(context, A.class);
				//context.startActivity(intent);
			}			
		}
		invalidate();
		return true;
	}
	
	private boolean isClick(MotionEvent event){
		if(event.getX(0) > (moveX - mDegree.getWidth() / 2) &&
		   event.getX(0) < (moveX + mDegree.getWidth() / 2) &&
		   event.getY(0) > (mGradientHeight + SPACING_HEIGHT - mTrendLine.getHeight())/2 &&
		   event.getY(0) < (mGradientHeight + SPACING_HEIGHT - mTrendLine.getHeight())/2 + mDegree.getHeight()){
			System.out.println("点到了");
			return true;
		}
		return false;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw");
		super.onDraw(canvas);
		Paint paint = new Paint();
		// 初始化绘制
		initDraw(canvas, paint);
		// 点击屏幕时 进行的操作, 单点，多点
		SinglePointTouch(canvas, paint);
	}

	/**
	 * 初始化绘制
	 * @param canvas
	 * @param paint
	 */
	private void initDraw(Canvas canvas, Paint paint) {
		Log.d(TAG, "initDraw()");
		paint.setColor(Color.GREEN);
		paint.setAntiAlias(true);
		/* 绘制虚线 */
		Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		PathEffect effects = new DashPathEffect(new float[] { 3, 3, 3, 3 }, 3);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(1);
		mPaint.setPathEffect(effects);
		
		float dottedSpacing = (mGradientHeight - WEIGHT) / 5;
		float smallDotted = dottedSpacing / 5;
		//TODO 记住
//		for (int i = 1; i <= 5; i++) {
//			Path dottedPath = new Path();
//			mPaint.setColor(Color.RED);
//			mPaint.setAlpha(0x50);
//			dottedPath.moveTo(SPACING, mGradientHeight + SPACING_HEIGHT - dottedSpacing * i);
//			dottedPath.lineTo(mGradientWidth, mGradientHeight + SPACING_HEIGHT - dottedSpacing * i);
//			canvas.drawPath(dottedPath, mPaint);
//		}
		/* 左边刻度尺 */
		for (int i = 0; i <= 5; i++) {
			paint.setStrokeWidth(1);
			canvas.drawText(new BigDecimal(2.6*i).toString(), SPACING - 25,mGradientHeight + SPACING_HEIGHT - dottedSpacing * i + 5,paint);
			canvas.drawLine(SPACING, mGradientHeight + SPACING_HEIGHT - dottedSpacing * i, mGradientWidth, mGradientHeight + SPACING_HEIGHT - dottedSpacing * i, paint);
			if(i != 0){
				for (int j = 1; j <= 4; j++) {
					//canvas.drawLine(SPACING, mGradientHeight + SPACING_HEIGHT - dottedSpacing * i + smallDotted * j,mGradientWidth, mGradientHeight + SPACING_HEIGHT - dottedSpacing * i + smallDotted * j,mPaint);
				}
			}			
		}

		/* 绘制曲线 覆盖 剪切后的锯齿 */
		for (int i = 0; i < points.size(); i++) {
			paint.setStrokeWidth(3);
			PointF startPoint = points.get(i);
			if (i + 1 == points.size()) {
				// 绘制最后一个圆点到底部的 竖线
				paint.setStrokeWidth(1);
				canvas.drawLine(startPoint.x, startPoint.y, startPoint.x,
						mGradientHeight + SPACING_HEIGHT, paint);
				// 绘制 最后一个圆点 为剪切的图片
				canvas.drawBitmap(mLastPoint,
						startPoint.x - mLastPoint.getWidth() / 2, startPoint.y
								- mLastPoint.getHeight() / 2, paint);
				break;
			}
			PointF endPoint = points.get(i + 1);
			// 绘制曲线，并且覆盖剪切后的锯齿
			canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y,paint);
			// 为了使线条圆滑 绘一个点 每个坐标系 绘制一个圆点
			canvas.drawPoint(startPoint.x, startPoint.y, paint);
		}

		// 绘制左边的竖线,以及温度的刻度 （由于需要与顶边 产生间距SPACING_HEIGHT）———— 目前模拟 待修改
		canvas.drawLine(SPACING, SPACING_HEIGHT, SPACING, mGradientHeight+ SPACING_HEIGHT, paint);
		paint.setTextSize(16);
		canvas.drawText("天", mGradientWidth + 5, mGradientHeight + SPACING_HEIGHT + 5, paint);
		canvas.drawText("度", SPACING - 8, 30, paint);
	}
	float moveX;
	/**
	 * 单点触控操作
	 * @param canvas
	 * @param paint
	 */
	private void SinglePointTouch(Canvas canvas, Paint paint) {
		Log.d(TAG, "SinglePointTouch()");
		// 顶部的度数框
		if (moveXOfFirst < points.get(0).x) {
			moveXOfFirst = points.get(0).x;
		}
		if (moveXOfFirst > points.get(points.size() - 1).x) {
			moveXOfFirst = points.get(points.size() - 1).x;
		}
		moveX = moveXOfFirst;
		// 绘制度数框 背景后的 横线
		//canvas.drawBitmap(mTrendLine, SPACING, mGradientHeight + SPACING_HEIGHT - mTrendLine.getHeight() + 5, paint);
		// 绘制 移动的 点
		onPointMove(canvas, paint, moveXOfFirst);
		canvas.drawBitmap(mDegree,moveX - mDegree.getWidth() / 2,
				(mGradientHeight + SPACING_HEIGHT - mTrendLine.getHeight())/2,paint);

		// 绘制 变动的 能耗为多少
		float moveY = getMoveY(moveXOfFirst);
		float energyHeight = (float) (mGradientHeight + SPACING_HEIGHT) - moveY;

		String energyText = String.valueOf(energyHeight / spacingOfY);
		// 为了避免误差 如果单点 手指在X轴 在预定的 X点上 那么直接将显示读书设置为 服务器传回的数据
		EnergyItem energy = isInPoint(moveXOfFirst);
		if (energy != null) {
			energyText = String.valueOf(energy.value);
		}
		int indexOf = energyText.indexOf(".");
		String substring = energyText.substring(0, indexOf + 2);
		paint.setTextSize(28);
		paint.setColor(Color.BLACK);
		canvas.drawText(substring + "度", moveX - mDegree.getWidth() / 3,
				mGradientHeight + SPACING_HEIGHT - mTrendLine.getHeight() + mDegree.getHeight() / 3, paint);
	}

	/**
	 * 点击绘制的 黄色的 移动圆点
	 */
	private void onPointMove(Canvas canvas, Paint paint, float moveX) {
		// 点住滑动 绘制的黄色线
		if (moveX < SPACING) {
			moveX = SPACING - mTrendLine.getWidth() / 2;
		}
		if (moveX > points.get(points.size() - 1).x) {
			moveX = points.get(points.size() - 1).x - mTrendLine.getWidth() / 2;
		}
		canvas.drawBitmap(mTrendLine,
				moveX - mTrendLine.getWidth() / 2, mGradientHeight
						+ SPACING_HEIGHT - mTrendLine.getHeight() + 5,
				paint);
		// 绘制移动中的点
		canvas.drawBitmap(mMovePoint,
				moveX - mMovePoint.getWidth() / 2, getMoveY(moveX)
						- mMovePoint.getWidth() / 2, paint);
	}
	/**
	 * 判断 目前手指在屏幕中X的点 是否在 结合点上
	 * @param moveX 手指移动中的点
	 * @return
	 */
	private EnergyItem isInPoint(float moveX) {
		EnergyItem energy = null;
		for (int i = 0; i < points.size(); i++) {
			if (moveX == points.get(i).x) {
				energy = energyItems.get(i);
				System.out.println(energy.date + ":" + energy.value);
				break;
			}
		}
		return energy;
	}
	/**
	 * 获取移动线条时 Y的值，
	 */
	private float getMoveY(float x) {
		float y = mGradientHeight + SPACING_HEIGHT;
		PointF first = null;
		PointF second = null;
		for (int i = 0; i < points.size() - 1; i++) {
			PointF point_1 = points.get(i);
			PointF point_2 = points.get(i + 1);
			if (point_1.x <= x && point_2.x >= x) {
				first = point_1;
				second = point_2;
				break;
			}
		}
		// 勾股定理 ：ｙ＝（ｙ２－ｙ１）／（ｘ２－ｘ１）＊（ｘ－ｘ１）＋ｙ１
		if (first != null || second != null)
			y = Math.abs((second.y - first.y) / (second.x - first.x)
					* (x - first.x) + first.y);
		return y;
	}
	/**
	 * 初始化 加载 view 的资源图片
	 */
	public void setImages() {
		Log.d(TAG, "setImages()");
		// 背景渐变色大图
		int windowW = (int) (dm.widthPixels - SPACING_SCALE);
		mGradientWidth = windowW;
		mGradientHeight = (float) (windowW*0.75);
		// 移动的黄色的线条
		mTrendLine = BitmapFactory.decodeResource(getResources(),R.drawable.energy_trendline);
		int height = mTrendLine.getHeight();
		float trendLineScale = (float) mGradientHeight / height;
		mTrendLine = scaleBmp(mTrendLine, 1.0f, trendLineScale); // 缩放图层
		// 最后一个灰色的点
		mLastPoint = BitmapFactory.decodeResource(getResources(), R.drawable.energy_trendpoint_last);
		mMovePoint = BitmapFactory.decodeResource(getResources(), R.drawable.energy_trendpoint_move);
		mDegree = BitmapFactory.decodeResource(getResources(), R.drawable.energy_degree);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "onMeasure(int widthMeasureSpec, int heightMeasureSpec)");
		Log.e("onMeasure", widthMeasureSpec + ":" + heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 图片缩放
	 * @param bitmap 需要缩放的图片
	 * @param scaleX X缩放比例
	 * @param scaleY Y缩放比例
	 * @return
	 */
	public Bitmap scaleBmp(Bitmap bitmap, float scaleX, float scaleY) {
		Log.d(TAG, "scaleBmp(Bitmap bitmap, float scaleX, float scaleY)");
		Bitmap scaleBmp = null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleX, scaleY);
		scaleBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,false);
		bitmap.recycle();
		return scaleBmp;
	}
	public void setWindowsWH(DisplayMetrics dm) {
		Log.d(TAG, "setWindowsWH(DisplayMetrics dm)");
		this.dm = dm;
		setImages();
	}

	
	/**
	 * 通过数据 预先存入需要绘制的 连线点
	 * @param powers
	 * @param date
	 */
	public void initPoints(ArrayList<EnergyItem> energys) {
		Log.d(TAG, "initPoints(ArrayList<EnergyItem> energys)");
		getSpacingOfXY(energys);
		points = new ArrayList<PointF>();
		for (int i = 0; i < energys.size(); i++) {
			float f = energys.get(i).value;
			float y = ((mGradientHeight + SPACING_HEIGHT) - f * spacingOfY);
			float x = (i * spacingOfX + SPACING);
			PointF point = new PointF(x, y);
			points.add(point);
			Log.i("initPoints", energys.get(i).date + "|point.x:" + x + "|point.y:" + y);
		}
	}

	/**
	 * 获取X的间距 以及 Y的间距
	 * @param powers
	 * @param date
	 */
	private void getSpacingOfXY(ArrayList<EnergyItem> energys) {
		Log.d(TAG, "getSpacingOfXY(ArrayList<EnergyItem> energys)");
		maxEnergy = findMaxPowers(energys);
		Log.i("maxEnergy", maxEnergy.value + "");
		spacingOfX = mGradientWidth / (energys.size()) + 1;
		spacingOfY = (mGradientHeight - WEIGHT) / ((maxEnergy.value) / 4 + maxEnergy.value);
		Log.e("spacingOfX and spacingOfY", spacingOfX + ":" + spacingOfY);
	}

	/**
	 * 找到 数据集合中 最高能量 对应的脚标
	 * @param powers
	 * @return
	 */
	private static EnergyItem findMaxPowers(ArrayList<EnergyItem> energys) {
		Log.d(TAG, "findMaxPowers(ArrayList<EnergyItem> energys)");
		EnergyItem energy = new EnergyItem();
		energy.value = 0;
		for (int i = 0; i < energys.size(); i++) {
			if (energys.get(i).value > energy.value) {
				energy = energys.get(i);
			}
		}
		return energy;
	}
	public void setData(ArrayList<EnergyItem> energyItems){
		Log.d(TAG, "setData(ArrayList<EnergyItem> energyItems,int i)");
		this.energyItems = energyItems;
	}
}