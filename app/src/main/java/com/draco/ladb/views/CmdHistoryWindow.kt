package com.draco.ladb.views

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.draco.ladb.R
import com.draco.ladb.viewmodels.MainActivityViewModel
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.TranslationConfig

class CmdHistoryWindow(
    private val ctx: Context,
    private var viewModel: MainActivityViewModel?
) : BasePopupWindow(ctx) {

    inner class ListAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_cmd_history) {

        override fun convert(holder: BaseViewHolder, item: String) {
            holder.setText(R.id.tv_cmd_history, item)
        }
    }

    private val mMaxHeight by lazy { dp2px(ctx, 256f) }
    private lateinit var mHistoryLyt: View
    private var onSelect: ((lastCmd: String) -> Unit)? = null

    init {
        showAnimation =
            AnimationHelper.asAnimation().withTranslation(TranslationConfig.FROM_TOP).toShow()
        dismissAnimation =
            AnimationHelper.asAnimation().withTranslation(TranslationConfig.TO_TOP).toDismiss()
        setAlignBackground(true)
        setAlignBackgroundGravity(Gravity.TOP)
        setContentView(R.layout.layout_cmd_history_window)
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        mHistoryLyt = findViewById(R.id.lyt_cmd_history)
        val listAdapter = ListAdapter().apply {
            setOnItemClickListener { _, _, position ->
                onSelect?.invoke(getItem(position))
                dismiss()
            }
        }
        findViewById<RecyclerView>(R.id.rv_cmd_history).apply {
            adapter = listAdapter
        }

        viewModel?.cmdHistoryData?.run {
            observe(ctx as LifecycleOwner) {
                listAdapter.setList(it?.reversed())
                updateHeight(mHistoryLyt)
            }
            listAdapter.setList(value?.reversed())
        }
    }

    fun show(anchorView: View, onSelect: (lastCmd: String) -> Unit) {
        val cmds = viewModel?.cmdHistoryData?.value ?: return
        if (cmds.isNotEmpty()) {
            this.onSelect = onSelect
            showPopupWindow(anchorView)
            updateHeight(mHistoryLyt)
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        onSelect = null
    }

    override fun onDestroy() {
        viewModel = null
        super.onDestroy()
    }

    private fun updateHeight(view: View?) {
        view?.run { post { if (height > mMaxHeight) updateLayoutParams { height = mMaxHeight } } }
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
