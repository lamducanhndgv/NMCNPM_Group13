package group13.ntphat.evernote.ui.notes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import group13.ntphat.evernote.Model.NOTE;
import group13.ntphat.evernote.Model.NOTEBOOK;
import group13.ntphat.evernote.Model.USER;
import group13.ntphat.evernote.R;
import xute.markdeditor.EditorControlBar;
import xute.markdeditor.MarkDEditor;
import xute.markdeditor.datatype.DraftDataItemModel;
import xute.markdeditor.models.DraftModel;
import xute.markdeditor.utilities.FilePathUtils;

import static xute.markdeditor.Styles.TextComponentStyle.BLOCKQUOTE;
import static xute.markdeditor.Styles.TextComponentStyle.H1;
import static xute.markdeditor.Styles.TextComponentStyle.H3;
import static xute.markdeditor.Styles.TextComponentStyle.NORMAL;
import static xute.markdeditor.components.TextComponentItem.MODE_OL;
import static xute.markdeditor.components.TextComponentItem.MODE_PLAIN;

public class ViewNoteActivity extends AppCompatActivity implements EditorControlBar.EditorControlListener  {
    private final int REQUEST_IMAGE_SELECTOR = 110;
    private MarkDEditor markDEditor;
    private DraftModel content;
    private EditorControlBar editorControlBar;
    private NOTE clickedNote;

    static public String notebookId;
    private EditText title;
    private TextView notebook;
    private boolean isNewNote;
    private Spinner spinner;
    ArrayList<String> nb_names = new ArrayList<>();
    ArrayList<String> nb_ids = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);

        isNewNote=false;
        Intent intentCatcher = getIntent();
        getClickedNote(intentCatcher);
        initComponent();
    }

    @Override
    public void onInsertImageClicked() {
        openGallery();
    }

    @Override
    public void onInserLinkClicked() {
        //markDEditor.addLink("Click Here", "http://www.hapramp.com");
    }

    private void getClickedNote(Intent catcher){
        String noteId = catcher.getStringExtra("noteid");
        if (noteId.equals("-1")){
            clickedNote = new NOTE();
            notebookId = USER.getInstance().getAllNoteBook().get(0).getNotebookID();
            isNewNote=true;
        }
        else{
            notebookId = catcher.getStringExtra("notebookid");
            clickedNote = USER.getInstance().getNote(notebookId, noteId);
            //notebookId = clickedNote.getNotebookID();
        }
    }

    private void initComponent(){
        title = findViewById(R.id.noteview_title);
        notebook = findViewById(R.id.noteview_notebook);
        spinner = findViewById(R.id.notebook_chooser);
        if (!isNewNote){
            content = new Gson().fromJson(clickedNote.getContent(), DraftModel.class);
            title.setText(clickedNote.getTitle());
            if (content == null){
                content = initDraftContent();
            }

        }
        else{
            ArrayList<NOTEBOOK> list_nb = USER.getInstance().getAllNoteBook();

            for (int i = 0; i <  list_nb.size(); i++){
                nb_names.add(list_nb.get(i).getNameNoteBook());
                nb_ids.add(list_nb.get(i).getNotebookID());
            }
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nb_names);
            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    notebookId = nb_ids.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    notebookId = nb_ids.get(0);
                }
            });
            content = initDraftContent();
        }
        String notebookName = USER.getInstance().getNoteBook(notebookId).getNameNoteBook();
        notebook.setText(notebookName);
        markDEditor = findViewById(R.id.mdEditor);
        markDEditor.configureEditor(
                "https://be4808e3.ngrok.io/uploader/",
                "",
                true,
                "Start Here...",
                BLOCKQUOTE
        );
        markDEditor.loadDraft(content);
        editorControlBar = findViewById(R.id.controlBar);
        editorControlBar.setEditorControlListener(this);
        editorControlBar.setEditor(markDEditor);
    }

    // Init note content
    private DraftModel initDraftContent() {
        ArrayList<DraftDataItemModel> contentTypes = new ArrayList<>();
        DraftDataItemModel heading = new DraftDataItemModel();
        heading.setItemType(DraftModel.ITEM_TYPE_TEXT);
        heading.setContent("take note here");
        heading.setMode(MODE_PLAIN);
        heading.setStyle(NORMAL);

        contentTypes.add(heading);
        DraftModel contentModel = new DraftModel(contentTypes);
        return contentModel;
    }

    private String getCurrentDateAsFormat(String format){
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat(format);
        String formattedDate = df.format(c);
        return formattedDate;
    }

    public void save_content(MenuItem item) {
        clickedNote.setContent(new Gson().toJson(markDEditor.getDraft()));
        clickedNote.setTitle(title.getText().toString());
        clickedNote.setCreateDate(getCurrentDateAsFormat("dd-MM-yyyy"));

        String newNotebookId = notebookId;
        if (clickedNote.getNoteID() != null)
            USER.getInstance().updateNote(newNotebookId, clickedNote);
        else
            USER.getInstance().addNote(newNotebookId, clickedNote);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECTOR) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                String filePath = FilePathUtils.getPath(this, uri);
                Log.d("MarkDEditor", filePath);
                addImage(filePath);
            }
        }
    }
    public void addImage(String filePath) {
        markDEditor.insertImage(filePath);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_IMAGE_SELECTOR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                    //Toast.makeText()"Permission not granted to access images.");
                }
                break;
        }
    }

    private void openGallery() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_SELECTOR);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_IMAGE_SELECTOR);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void choose_notebook(View view) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return true;
    }

    public String getNotebookId(){
        return notebookId;
    }

}
