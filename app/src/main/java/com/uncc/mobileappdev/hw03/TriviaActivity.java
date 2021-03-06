package com.uncc.mobileappdev.hw03;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Created by CHINU on 2/19/2018.
 */


import java.util.ArrayList;

public class TriviaActivity extends AppCompatActivity implements DownloadQuestionPicture.IQuestionPicture {
    private ArrayList<Question> questions;
    ArrayList<String> choices;
    private int questionIndex = 0;
    private int correctAnswersCount = 0;
    boolean isCountdownRunning = false;

    private TextView txtViewQuestionNo;
    private TextView txtViewTimeLeft;
    private ImageView imgViewQuestionPic;
    private TextView txtViewQuestion;
    private RadioGroup rdGrpOptions;
    private Button btnQuit;
    private Button btnNext;
    private View layout_image_loading;

    private static CountDownTimer countDownTimer;
    private Intent intent;

    public static final String TOTAL_QUES_COUNT_KEY = "TOTAL_QUES_COUNT";
    public static final String CORRECT_ANS_COUNT_KEY = "CORRECT_ANS_COUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        txtViewQuestionNo = (TextView) findViewById(R.id.text_view_question_no);
        txtViewTimeLeft = (TextView) findViewById(R.id.text_view_time_left);
        imgViewQuestionPic = (ImageView) findViewById(R.id.image_view_question_picture);
        txtViewQuestion = (TextView) findViewById(R.id.text_view_question);
        rdGrpOptions = (RadioGroup) findViewById(R.id.radio_group_options);
        btnQuit = (Button) findViewById(R.id.action_quit);
        btnNext = (Button) findViewById(R.id.action_next);
        layout_image_loading = findViewById(R.id.layout_image_loading);

        if (getIntent().getExtras() != null) {
            questions = (ArrayList<Question>) getIntent().getExtras().getSerializable(MainActivity.QUESTIONS_LIST_KEY);
        }

        setTriviaUIElements(questionIndex);
        if(!isCountdownRunning) {
            countDownTimer = new CountDownTimer((120 * 1000), (1000)) {
                @Override
                public void onTick(long millisUntilFinished) {
                    txtViewTimeLeft.setText(getResources().getString(R.string.text_view_time_left, (millisUntilFinished / 1000)));
                }

                @Override
                public void onFinish() {
                    loadStatActivity();
                }
            }.start();
            isCountdownRunning = true;
        }
    }

    @Override
    public void setupData(Bitmap image, int questionIndex) {
        if(image == null){
            imgViewQuestionPic.setImageResource(R.drawable.trivia);
            layout_image_loading.setVisibility(View.INVISIBLE);
        }

        if (this.questionIndex == questionIndex) {
            imgViewQuestionPic.setImageBitmap(image);
        }
    }

    @Override
    public void startProgress() {
        layout_image_loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopProgress(int questionIndex) {
        if (this.questionIndex == questionIndex)
            layout_image_loading.setVisibility(View.INVISIBLE);
    }

    /**
     * Handler for quit button
     * @param view
     */
    public void quitAction(View view) {
        finish();
    }

    /**
     * Handler for next button
     * @param view
     */
    public void nextAction(View view) {
        if (rdGrpOptions.getCheckedRadioButtonId() == -1) {
            Toast.makeText(TriviaActivity.this, getResources().getString(R.string.error_no_option_selected), Toast.LENGTH_SHORT).show();
        } else {
            checkAnswer(questionIndex);

            questionIndex++;
            if (questionIndex < questions.size()) {
                setTriviaUIElements(questionIndex);
            } else {
                loadStatActivity();
            }
        }
    }

    /**
     * Update Trivia Activity elements
     * @param questionIndex
     */
    private void setTriviaUIElements(int questionIndex) {
        txtViewQuestionNo.setText(getResources().getString(R.string.text_view_question_no, (questionIndex + 1)));

        imgViewQuestionPic.setImageBitmap(null);
        String url = questions.get(questionIndex).getImageURL();
        if (url != null) {
            new DownloadQuestionPicture(this).execute(url, String.valueOf(questionIndex));
        } else {
            layout_image_loading.setVisibility(View.INVISIBLE);
        }

        txtViewQuestion.setText(questions.get(questionIndex).getQuestion());
        choices = questions.get(questionIndex).getAnswers();
        RadioButton rdBtnOption;
        rdGrpOptions.clearCheck();
        rdGrpOptions.removeAllViews();
        for (int index = 0; index < choices.size(); index++) {
            rdBtnOption = new RadioButton(TriviaActivity.this);
            rdBtnOption.setText(choices.get(index));
            rdBtnOption.setId(index + 1);
            rdGrpOptions.addView(rdBtnOption);
        }
    }

    /**
     * Keeps track of right and wrong answers
     * @param questionIndex
     */
    private void checkAnswer(int questionIndex) {
        int usersAns = (rdGrpOptions.getCheckedRadioButtonId()) - 1; //Zero-based index
        int originalAns = questions.get(questionIndex).getAnswerIndex();

        if(usersAns == originalAns) {
            correctAnswersCount++;
        }
    }

    /**
     * Loads stats activity
     */
    private void loadStatActivity() {
        countDownTimer.cancel();
        isCountdownRunning = false;
        intent = new Intent(TriviaActivity.this, StatsActivity.class);
        intent.putExtra(MainActivity.QUESTIONS_LIST_KEY, questions);
        intent.putExtra(CORRECT_ANS_COUNT_KEY, correctAnswersCount);
        startActivity(intent);
        finish();
    }
}
