package com.itis.android_homework.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.itis.android_homework.R
import com.itis.android_homework.recycler_view.TaskAdapter
import com.itis.android_homework.callbacks.SwipeToDeleteCallback
import com.itis.android_homework.data.AppDatabase
import com.itis.android_homework.data.entity.Task
import com.itis.android_homework.databinding.MainFragmentBinding
import com.itis.android_homework.item_decorator.SpaceItemDecorator

class MainFragment : Fragment(R.layout.main_fragment) {

    private var binding: MainFragmentBinding? = null
    private var taskAdapter: TaskAdapter? = null
    private lateinit var database: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainFragmentBinding.bind(view)
        database = AppDatabase.invoke(context) as AppDatabase

        taskAdapter = TaskAdapter({ showTaskFragment(it) }, { deleteTask(it) })

        binding?.apply {
            toolbar.setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
            fabAdd.setOnClickListener {
                showTaskFragment(null)
            }
            rvTasks.run {
                adapter = taskAdapter
                addItemDecoration(
                    DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
                )
                addItemDecoration(SpaceItemDecorator(context))

                ItemTouchHelper(SwipeToDeleteCallback{
                    deleteTask(it)
                }).attachToRecyclerView(this)
            }
        }

        val tasks = database.taskDao().getAllTasks()
        updateTasks(tasks)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.icon_delete_all -> {
                deleteAllTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateTasks(newList: List<Task>) {
        binding?.apply {
            if (newList.isEmpty()) {
                tvNoTasksAdded.visibility = View.VISIBLE
                rvTasks.visibility = View.GONE
            } else {
                tvNoTasksAdded.visibility = View.GONE
                rvTasks.visibility = View.VISIBLE
            }
        }
        taskAdapter?.submitList(ArrayList(newList))
    }

    private fun deleteAllTasks() {
        if(binding?.rvTasks?.visibility == View.VISIBLE) {
            database.taskDao().deleteAllTasks()
            showMessage("Successfully deleted all tasks.")
        } else showMessage("You have no tasks to be deleted.")
    }

    private fun deleteTask(id: Int) {
        database.taskDao().deleteTaskById(id)
        showMessage("Task successfully deleted.")
    }

    private fun showTaskFragment(id: Int?) {
        var bundle: Bundle? = null
        id?.also {
            bundle = Bundle().apply {
                putInt("ARG_TASK_ID", id)
            }
        }
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.enter_from_right)
            .setExitAnim(R.anim.exit_to_left)
            .setPopEnterAnim(R.anim.enter_from_left)
            .setPopExitAnim(R.anim.exit_to_right)
            .build()

        findNavController().navigate(
            R.id.action_mainFragment_to_taskFragment,
            bundle,
            options
        )
    }

    private fun showMessage(message: String) {
        Snackbar.make(
            requireActivity().findViewById(R.id.container),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
