package ui

import database.DeadlineFilter
import database.Item
import database.TimeFilter
import java.awt.Component
import javax.swing.*


sealed class ViewMode {
    object Empty : ViewMode()
    object Categories : ViewMode()
    data class TasksByTime(val filter: TimeFilter) : ViewMode()
    data class TasksByCategory(val categoryId: Int) : ViewMode()
    data class TasksByDeadline(val filter: DeadlineFilter) : ViewMode()
}

class TitleListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val label = super.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus
        ) as JLabel

        label.text = when (value) {
            is Item -> value.title
            else -> value?.toString() ?: ""
        }

        return label
    }
}
