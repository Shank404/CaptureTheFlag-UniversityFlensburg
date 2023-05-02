package de.hsfl.team34.capturetheflag

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject

class MapView(context: Context, attr: AttributeSet) : View(context, attr) {

    var gesturePoint: MutableLiveData<JSONObject> = MutableLiveData()
    var pointsCollection: MutableList<JSONObject> = mutableListOf()
    var mapViewSize: MutableLiveData<JSONObject> = MutableLiveData()

    var pointsCollectionServer: MutableList<JSONObject> = mutableListOf()
    var locationPlayer: JSONObject = JSONObject(mapOf("lat" to 1.0, "long" to 1.0))

    var drawPointCollectionSever: Boolean = false
    var drawPointCollection: Boolean = true
    var drawPlayer: Boolean = false
    var drawPlayerPointer: Boolean = false

    private val mapDrawable = AppCompatResources.getDrawable(context, R.drawable.campuskarte)!!
    private val mapBitmap = (mapDrawable as BitmapDrawable).bitmap
    var mapMatrix = Matrix()


    private val playerFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private val pointFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }
    private val pointerFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }
    private val blackStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    init {
        minimumWidth = 25
        minimumHeight = 25
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            it.drawBitmap(mapBitmap, mapMatrix, null)

            drawAtPosition(it, locationPlayer.getDouble("long") * width, locationPlayer.getDouble("lat") * height, playerFill, "player", drawPlayer)
            drawAtPosition(it, locationPlayer.getDouble("long") * width, locationPlayer.getDouble("lat") * height, pointerFill, "playerPointer", drawPlayerPointer)

            if (drawPointCollection) {
                for (point in pointsCollection) {
                    if(point.getBoolean("calculate")){
                        drawAtPosition(it, point.getDouble("long") * width, point.getDouble("lat") * height, pointFill, "point", drawPointCollection)
                    } else {
                        drawAtPosition(it, point.getDouble("long"), point.getDouble("lat"), pointFill, "point", drawPointCollection)
                    }
                }
            }

            if (drawPointCollectionSever) {
                for (point in pointsCollectionServer) {
                    if(point.getDouble("lat") < 1){
                        drawAtPosition(it, point.getDouble("long") * width, point.getDouble("lat") * height, changePointFill(point.getInt("team")), "point", drawPointCollectionSever)
                    } else {
                        drawAtPosition(it, point.getDouble("long"), point.getDouble("lat") + 159.0, changePointFill(point.getInt("team")), "point", drawPointCollectionSever)
                    }
                }
            }
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mapViewSize.value = JSONObject(mapOf("width" to width, "height" to height))
        mapMatrix = Matrix().apply {
            setScale(w / mapBitmap.width.toFloat(), h / mapBitmap.height.toFloat())
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = minimumWidth + paddingLeft + paddingRight
        val finalW = resolveSize(minW, widthMeasureSpec)

        val minH = minimumHeight + paddingTop + paddingBottom
        val finalH = resolveSize(minH, heightMeasureSpec)
        setMeasuredDimension(finalW, finalH)
    }

    private fun drawAtPosition(
        it: Canvas,
        posX: Double,
        posY: Double,
        color: Paint,
        shapeOf: String,
        draw: Boolean
    ) {
        if (draw && shapeOf == "player") {
            it.drawCircle(posX.toFloat(), posY.toFloat(), 25f, color)
            it.drawCircle(posX.toFloat(), posY.toFloat(), 25f, blackStroke)
        }
        if (draw && shapeOf == "point") {
            it.drawCircle(posX.toFloat(), posY.toFloat(), 25f, color)
            it.drawCircle(posX.toFloat(), posY.toFloat(), 25f, blackStroke)
        }
        if (draw && shapeOf == "playerPointer") {
            it.drawArc(posX.toFloat() - 80f, posY.toFloat() - 120f, posX.toFloat() + 80f, posY.toFloat() + 60f, 240f, 60f, true, color)
            it.drawArc(posX.toFloat() - 80f, posY.toFloat() - 120f, posX.toFloat() + 80f, posY.toFloat() + 60f, 240f, 60f, true, blackStroke)
        }
    }

    private fun changePointFill(value: Int): Paint {
        var pointFill = Paint(Paint.ANTI_ALIAS_FLAG)
        when (value) {
            -1 -> {
                pointFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.YELLOW
                    style = Paint.Style.FILL
                }
            }
            0 -> {
                pointFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GRAY
                    style = Paint.Style.FILL
                }
            }
            1 -> {
                pointFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                }
            }
            2 -> {
                pointFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLUE
                    style = Paint.Style.FILL
                }
            }
        }
        return pointFill
    }

    fun createGestureListener(): GestureDetector.SimpleOnGestureListener {
        return object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) {
                gesturePoint.value = JSONObject(mapOf("long" to e!!.x, "lat" to e!!.y))
            }
        }
    }

}