package io.vn.catan.filemanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class MyAdapter(
    private var context: Context,
    private var filesAndFolders: Array<File>,
    private var reload: (() -> Unit)
) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    private fun renameFileWithoutPath(filePath: String, newFileName: String): Boolean {
        val path = FileSystems.getDefault().getPath(filePath)
        val parentDir = path.parent
        val newPath = if (parentDir != null) {
            parentDir.resolve(newFileName)
        } else {
            FileSystems.getDefault().getPath(newFileName)
        }

        try {
            Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING)
            println("File renamed successfully.")
        } catch (e: Exception) {
            println("Error renaming file: ${e.message}")
            return false
        }
        return true
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selectedFile = filesAndFolders[position]
        holder.textView.text = selectedFile.name
        if (selectedFile.isDirectory) {
            holder.imageView.setImageResource(R.drawable.ic_baseline_folder_24)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24)
        }
        holder.itemView.setOnClickListener {
            if (selectedFile.isDirectory) {
                val intent = Intent(context, ListFilesActivity::class.java)
                val path = selectedFile.absolutePath
                intent.putExtra("path", path)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                // TODO: open the file


            }
        }
        holder.itemView.setOnLongClickListener { v ->
            val popupMenu = PopupMenu(context, v)
            popupMenu.menu.add("DELETE")
            popupMenu.menu.add("MOVE")
            popupMenu.menu.add("RENAME")
            popupMenu.setOnMenuItemClickListener { item ->
                if (item.title == "DELETE") {
                    AlertDialog.Builder(context)
                        .setTitle("Delete ${if (selectedFile.isDirectory) "folder" else "file"}")
                        .setMessage("Are you sure to delete this?")
                        .setPositiveButton("OK") { _, _ ->
                            val deleted = selectedFile.delete()
                            if (deleted) {
                                Toast.makeText(
                                    context.applicationContext,
                                    "DELETED",
                                    Toast.LENGTH_SHORT
                                ).show()
                                v.visibility = View.GONE
                            }
                            reload()
                        }
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show()

                }
                if (item.title == "MOVE") {
                    Toast.makeText(context.applicationContext, "MOVED", Toast.LENGTH_SHORT)
                        .show()
                }
                if (item.title == "RENAME") {
                    val view =
                        LayoutInflater.from(context).inflate(R.layout.rename_dialog, null, false)
                    val input = view.findViewById<EditText>(R.id.rename_input)
                    input.setText(selectedFile.nameWithoutExtension)

                    AlertDialog.Builder(context)
                        .setTitle("Rename ${if (selectedFile.isDirectory) "folder" else "file"}")
                        .setView(view)
                        .setPositiveButton("OK") { _, _ ->
                            val target = input.text.toString() + "." + selectedFile.extension
                            val renamed = renameFileWithoutPath(selectedFile.absolutePath, target)
                            if (renamed) {
                                Toast.makeText(
                                    context.applicationContext,
                                    "RENAMED",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                reload()
                            } else {
                                Toast.makeText(
                                    context.applicationContext,
                                    "FAILED",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show()
                }
                true
            }
            popupMenu.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return filesAndFolders.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView
        var imageView: ImageView

        init {
            textView = itemView.findViewById<TextView>(R.id.file_name_text_view)
            imageView = itemView.findViewById<ImageView>(R.id.icon_view)
        }
    }
}