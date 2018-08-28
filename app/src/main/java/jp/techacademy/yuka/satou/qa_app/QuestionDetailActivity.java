package jp.techacademy.yuka.satou.qa_app;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private FloatingActionButton mBookmarkButton;
    private DatabaseReference mAnswerRef;
    private DatabaseReference mDataBaseReference;

    boolean mFavoriteFlag = false;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();


            for (Answer answer : mQuestion.getAnswers()) {
                //同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getmAnswerUid())) {
                    return;
                }
            }
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mFavoriteFlag = true;
            mBookmarkButton.setImageResource(R.drawable.star_pressed);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        //渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        //ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mBookmarkButton = (FloatingActionButton) findViewById(R.id.bookmarkButton);
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        mBookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference favoriteRef = mDataBaseReference.child(Const.favoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                if (mFavoriteFlag == false) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("Genre", String.valueOf(mQuestion.getGenre()));
                    favoriteRef.setValue(data);
                    mFavoriteFlag = true;
                    mBookmarkButton.setImageResource(R.drawable.star_pressed);
                } else {
                    favoriteRef.removeValue();
                    mFavoriteFlag = false;
                    mBookmarkButton.setImageResource(R.drawable.star);
                }


            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    //ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);

                } else {
                    //Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // ログインしていなければお気に入りボタンを非表示にする
            mBookmarkButton.setVisibility(View.GONE);
        } else {
            mBookmarkButton.setVisibility(View.VISIBLE);
            DatabaseReference favoriteRef = mDataBaseReference.child(Const.favoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            favoriteRef.addChildEventListener(mFavoriteListener);

        }
    }
}
