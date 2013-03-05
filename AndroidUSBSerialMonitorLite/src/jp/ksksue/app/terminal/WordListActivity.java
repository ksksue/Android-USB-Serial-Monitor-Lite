
package jp.ksksue.app.terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class WordListActivity extends Activity {
    private ListView lvWord;
    private ArrayAdapter<String> mAdapter;
    private EditText etInputWord;

    CsvManager mCsv;
    private static final String WORDLIST_FILENAME = "wordlist.csv";

    private static final int DIALOG_DELETE          = 0;
    private static final int DIALOG_MOVE_TO_TOP     = 1;
    private static final CharSequence[] mDialogList = { "Delete",
                                                        "Move to the Top of the List"};

    private Builder mDialogBuilder;
    private String mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!getWindow().hasFeature(Window.FEATURE_ACTION_BAR)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.wordlist);

        etInputWord = (EditText) findViewById(R.id.etInputWord);

        mDialogBuilder = new Builder(this);

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mCsv = new CsvManager();
        mCsv.open(WORDLIST_FILENAME,this);
        mCsv.copyCsvToArrayAdapter(mAdapter);

        lvWord = (ListView) findViewById(R.id.lvWord);
        lvWord.setAdapter(mAdapter);

        lvWord.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {

                        ListView lv = (ListView) parent;
                        String word = lv.getItemAtPosition(position).toString();
                        Intent intent = getIntent();
                        intent.putExtra("word", word);
                        setResult(RESULT_OK, intent);
                        finish();

                    }
                });

        lvWord.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    public boolean onItemLongClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {
                        ListView lv = (ListView) parent;
                        mSelectedItem = lv.getItemAtPosition(position).toString();
                        mDialogBuilder
                        .setItems(mDialogList, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which) {
                                    case DIALOG_DELETE :
                                        mAdapter.remove(mSelectedItem);
                                        break;
                                    case DIALOG_MOVE_TO_TOP :
                                        mAdapter.remove(mSelectedItem);
                                        mAdapter.insert(mSelectedItem,0);
                                        break;
                                    default :
                                        break;
                                    }
                                }
                        });

                        mDialogBuilder.setCancelable(true);

                        AlertDialog alertDialog = mDialogBuilder.create();
                        alertDialog.show();

                        return true;

                    }
                });
    }

    public void onAddClick(View view) {
        String inputWord = etInputWord.getText().toString();
        if (inputWord.equals("")) {
            return;
        }

        mAdapter.remove(inputWord);     // delete a duplicate word
        mAdapter.insert(inputWord,0);

    }

    @Override
    protected void onPause(){
        super.onPause();
        mCsv.copyArrayAdapterToCsv(mAdapter,WORDLIST_FILENAME,1,this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mCsv.close();
    }
}
