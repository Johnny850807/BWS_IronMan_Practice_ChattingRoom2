package com.ood.waterball.teampathy.Fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ood.waterball.teampathy.Controllers.Global;
import com.ood.waterball.teampathy.DomainModels.WBS.TaskGroup;
import com.ood.waterball.teampathy.DomainModels.WBS.TaskItem;
import com.ood.waterball.teampathy.DomainModels.WBS.TodoTask;
import com.ood.waterball.teampathy.DomainModels.WBS.WbsVisitor;
import com.ood.waterball.teampathy.DomainModels.WBS.XmlTranslator;
import com.ood.waterball.teampathy.DomainModels.WBS.XmlTranslatorImp;
import com.ood.waterball.teampathy.Fragments.Architecture.AsyncTemplateFragment;
import com.ood.waterball.teampathy.R;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;

import static com.ood.waterball.teampathy.Controllers.MyLog.Log;


public class TaskAnalysisFragment extends AsyncTemplateFragment<String> implements WbsVisitor {
    private int projectId;
    private XmlTranslator xmlTranslator = new XmlTranslatorImp();
    private TaskItem taskRoot;
    private String wbsXml;
    private TaskItemFactory taskItemFactory;
    private boolean wbs_updated = false;

    private FlowLayout taskPanelView;
    private Button finishBtn;

    public static TaskAnalysisFragment getInstance(int projectId){
        TaskAnalysisFragment fragment = new TaskAnalysisFragment();
        Bundle args = new Bundle();
        args.putInt("projectId",projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_task_analysis;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected List<String> onFetchData(@Nullable Bundle arguBundle) {
        try {
            projectId = arguBundle.getInt("projectId");
            wbsXml = Global.getOfficeController().getTaskAnalysis(projectId);
            taskRoot = xmlTranslator.xmlToTasks(wbsXml);
            Log(wbsXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // here we need no list just only to get the xml string
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.go_see_charts_menu_on_toolbar,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == R.id.watch_charts_menu )
        {
            if (wbs_updated)
                createDialogForSureToUpdateTaskAnaylsis(true);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onFindViews(View parentView, List<String> entityList) {
        taskPanelView = (FlowLayout) parentView.findViewById(R.id.panel_flowlayout_taskAnalysis);
        finishBtn = (Button) parentView.findViewById(R.id.save_btn_taskAnalysis);
    }

    @Override
    protected void onControlViews() {
        taskItemFactory = new TaskItemFactory(getContext(),this,taskPanelView);
        createAllTaskItemViews();
        setFinishBtnListener();
    }

    private void createAllTaskItemViews() {
        for (TaskItem t : taskRoot)
            taskPanelView.addView(taskItemFactory.createItemView(t));
    }

    private void setFinishBtnListener(){
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialogForSureToUpdateTaskAnaylsis(false);
            }
        });
    }

    private void createDialogForSureToUpdateTaskAnaylsis(final boolean goWatchChartsAfterSave){
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.update))
                .setMessage(R.string.sure_to_update_task_analysis)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateTaskAnalysis();
                        if (goWatchChartsAfterSave)
                        //todo 前往 觀看工作分析圖 頁面
                        ;
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .show();
    }


    @Override
    public void taskViewOnClick(TaskGroup TaskGroup) {
        //todo show the options for choosing to add a task or to add a taskgroup

    }

    public void appendChildToGroup(TaskItem taskItem, TaskItem taskGroup) throws Exception {
        taskGroup.addTaskChild(taskItem);
        wbsXml = xmlTranslator.taskToXml(taskRoot);
        taskPanelView.removeAllViews();
        createAllTaskItemViews();
    }

    public void updateTaskAnalysis(){
        new Thread(){
            @Override
            public void run() {
                try {
                    Global.getOfficeController().updateTaskAnalysis(wbsXml, projectId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void taskViewOnClick(TodoTask task) {
        //todo show a dialog which shows the details of task item
        Toast.makeText(getContext(),task.getName(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void taskViewOnLongClick(TaskGroup TaskGroup) {
        //todo show a dialog for editting or deleting the taskgroup
    }

    @Override
    public void taskViewOnLongClick(TodoTask task) {
        //todo show a dialog for editting or deleting the task item
    }
}
