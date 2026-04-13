import database.getConnection
import database.ToDoDataAccessObject
import ui.ContentPanel
import ui.CreateEntityPanel
import ui.Sidebar

import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities



fun main() {
    SwingUtilities.invokeLater {
        val conn = getConnection()
        val dao = ToDoDataAccessObject(conn)

        val frame = JFrame("To Do List App")
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setSize(500, 400)
        frame.isVisible = true
        frame.setLocationRelativeTo(null)

        val rootPanel = JPanel(BorderLayout())
        frame.add(rootPanel, BorderLayout.CENTER)

        val contentPanel = ContentPanel(dao)
        rootPanel.add(contentPanel, BorderLayout.CENTER)

        val sidebar = Sidebar(
            Color.PINK,
            dao,
            contentPanel
        )
        rootPanel.add(sidebar, BorderLayout.WEST)

        val createEntityPanel = CreateEntityPanel(dao)
        createEntityPanel.background = Color.CYAN
        rootPanel.add(createEntityPanel, BorderLayout.EAST)

    }
}
