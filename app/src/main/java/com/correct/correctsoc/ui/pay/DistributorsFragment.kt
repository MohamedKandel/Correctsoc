package com.correct.correctsoc.ui.pay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentDistributorsBinding
import com.correct.correctsoc.helper.HelperClass
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class DistributorsFragment : Fragment() {

    private lateinit var binding: FragmentDistributorsBinding
    private lateinit var helper: HelperClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDistributorsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // action for swiped item
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
//                val icon = getTintedDrawable(requireContext(), R.drawable.phone_icon, R.color.white)

                val typeface = ResourcesCompat.getFont(requireContext(),R.font.barlow_semi_condensed_semibold)

                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.safe_color
                        )
                    )
                    .addSwipeRightActionIcon(R.drawable.phone_icon)
                    .setSwipeRightActionIconTint(R.color.white)
                    .addSwipeRightLabel(resources.getString(R.string.call))
                    .setSwipeRightLabelColor(R.color.white)
                    .setSwipeRightLabelTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
                    .setSwipeRightLabelTypeface(typeface)

                super.onChildDraw(
                    c, recyclerView,
                    viewHolder, dX, dY,
                    actionState, isCurrentlyActive
                )
            }

        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.contactsRecyclerView)

        return binding.root
    }

    /*private fun getTintedDrawable(context: Context, drawableId: Int, color: Int): Int {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return drawableId
        drawable.mutate().setTint(ContextCompat.getColor(context, color))

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDrawable(context.resources, bitmap).toBitmapDrawableResource(context)
    }

    private fun BitmapDrawable.toBitmapDrawableResource(context: Context): Int {
        // Generate a unique ID for the new drawable
        val resourceId = View.generateViewId()
        // You need to save this drawable in a way that it can be referenced by its resource ID.
        // For simplicity, this example just returns the original drawable ID.
        // Implement a mechanism to save and retrieve the drawable by ID.
        return resourceId
    }*/

}