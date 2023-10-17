import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.R  // Importe o recurso de cores do seu projeto

class HighlightItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val highlightPaint = Paint()

    init {
        val highlightColor = ContextCompat.getColor(context, R.color.highlight_color)
        highlightPaint.color = highlightColor
        highlightPaint.style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position != RecyclerView.NO_POSITION && position == highlightedPosition) {
                // Destacar o item com a cor desejada
                c.drawRect(
                    child.left.toFloat(),
                    child.top.toFloat(),
                    child.right.toFloat(),
                    child.bottom.toFloat(),
                    highlightPaint
                )
            }
        }
    }

    private var highlightedPosition: Int = RecyclerView.NO_POSITION

    fun highlightPosition(position: Int) {
        highlightedPosition = position
    }
}
