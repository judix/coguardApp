package com.app.coguardapp;


import android.app.Dialog;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;

import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import android.util.Log;

import android.view.Gravity;

import android.view.MenuItem;

import android.view.View;
import android.view.WindowManager;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.SessionResponse;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{


    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend , btnRecord;

    StreamPlayer streamPlayer = new StreamPlayer();
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200 , RECORD_REQUEST_CODE = 101;
    private static String TAG = "MainActivity";
    private boolean listening = false;
    private MicrophoneInputStream capture;
    private Context mContext;
    private MicrophoneHelper microphoneHelper;

    private Assistant watsonAssistant;
    private Response<SessionResponse> watsonAssistantSession;

    private ArrayList<TeyitPosts> listPosts;

    SharedPreferences sharedPrefs;
    boolean answerClick;

    DrawerLayout drawerLayout;
    Dialog dialog;
    NavigationView navigationView;
    ImageView menuOpen, refesh;
    int streak_current_user = 0;
    ImageView logo ;


    private void createServices() {
        watsonAssistant = new Assistant("2019-02-28", new IamAuthenticator(mContext.getString(R.string.assistant_apikey)));
        watsonAssistant.setServiceUrl(mContext.getString(R.string.assistant_url));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = getApplicationContext().getSharedPreferences("answer", MODE_PRIVATE);
        answerClick = sharedPrefs.getBoolean("clicked", true);
       // sharedPrefs.edit().remove("clicked").apply();
        Animation top = AnimationUtils.loadAnimation(this,R.anim.top);


        drawerLayout=  findViewById(R.id.drawer);
        navigationView =  findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);


        refesh = findViewById(R.id.refreshh);

        refesh.setVisibility(View.INVISIBLE);
        refesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(getIntent());

            }
        });

        menuOpen = findViewById(R.id.menuOpen);
        menuOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });


        logo = findViewById(R.id.logo);
        logo.setAnimation(top);

        showDialog();
        listPosts = new ArrayList<>();
        mContext = getApplicationContext();


        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        btnRecord = findViewById(R.id.btn_record);
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);
        recyclerView = findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<Message>();
        mAdapter = new ChatAdapter(messageArrayList, MainActivity.this.getApplicationContext());
        microphoneHelper = new MicrophoneHelper(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Message audioMessage = (Message) messageArrayList.get(position);

                //if(messageArrayList.get(position).toString().contains("Evde"))

                if (audioMessage != null && !audioMessage.getMessage().isEmpty()) {
                    new SayTask().execute(audioMessage.getMessage());
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                refesh.setVisibility(View.VISIBLE);



                sendMessage();



                if (checkInternetConnection()) {
                }

            }
        });




        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Bu özellik çok yakında sizlerle...", Toast.LENGTH_SHORT).show();
            }
        });



        createServices();
        sendMessage();
        retrieveTeyit();
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.esayı:
                inputMessage.setText("Enfekte sayısı nedir");
                break;
            case R.id.ilac:
                inputMessage.setText("Corona virüsüne karşı bir ilaç veya aşı var mıdır");
                break;
            case R.id.etki:
                inputMessage.setText("Virüs kimleri daha çok etkiliyor");
                break;
            case R.id.ani:
                inputMessage.setText("Virüs ani ölüme neden oluyor mu");
                break;
            case R.id.sempton:
                inputMessage.setText("Virüsün semptomları neler");
                break;
            case R.id.evcil:
                inputMessage.setText("Virüs evcil hayvanlardan bulaşıyor mu");
                break;
            case R.id.kargo:
                inputMessage.setText("Yurt dışından gelen kargo paketlerinden virüs geçer mi");
                break;

            case R.id.anti:
                inputMessage.setText("Antibiyotik virüse karşı etkili mi");
                break;
            case R.id.vkoru:
                inputMessage.setText("Bağışıklık sistemimi nasıl güçlendiririm");
                break;

            case R.id.kılavuz:
                inputMessage.setText("Sıkça kullanılan terimlerin anlamları");
                break;

            case R.id.enfekte:
                inputMessage.setText("Enfektemiyim?");
                break;
            case R.id.maskek:
                inputMessage.setText("Maske nasıl kullanılır?");
                break;
            case R.id.maskek1:
                inputMessage.setText("Maske çeşiteri neler");
                break;
            case R.id.maskek2:
                inputMessage.setText("Maskelerin özellikleri nedir");
                break;

            case R.id.maskek3:
                inputMessage.setText("Maskeleri ücretsiz nasıl temin edebilirim");

                break;
            case R.id.Oyun:
                inputMessage.setText("Oyun oynamaya ne dersin");
                break;
        }


        return true;
    }

    // Sending a message to Watson Assistant Service
    private void sendMessage() {

        final String inputmessage = this.inputMessage.getText().toString().trim();


        if (!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
        } else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            this.initialRequest = false;
//            Toast.makeText(getApplicationContext(), "Tap on the message for Voice", Toast.LENGTH_LONG).show();

        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();

        if(CheckQuestionIsIncludedInTeyit(inputmessage) != null)
            ResponseByTeyit(CheckQuestionIsIncludedInTeyit(inputmessage));

        else{



            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        if (watsonAssistantSession == null) {
                            ServiceCall<SessionResponse> call = watsonAssistant.createSession(new CreateSessionOptions.Builder().assistantId(mContext.getString(R.string.assistant_id)).build());
                            watsonAssistantSession = call.execute();
                        }

                        MessageInput input = new MessageInput.Builder()
                                .text(inputmessage)
                                .build();
                        MessageOptions options = new MessageOptions.Builder()
                                .assistantId(mContext.getString(R.string.assistant_id))
                                .input(input)
                                .sessionId(watsonAssistantSession.getResult().getSessionId())
                                .build();
                        Response<MessageResponse> response = watsonAssistant.message(options).execute();
                        Log.i(TAG, "run: " + response.getResult());
                        if (response != null &&
                                response.getResult().getOutput() != null &&
                                !response.getResult().getOutput().getGeneric().isEmpty()) {

                            List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();

                            for (RuntimeResponseGeneric r : responses) {
                                Message outMessage;
                                switch (r.responseType()) {
                                    case "text":
                                        outMessage = new Message();
                                        outMessage.setMessage(r.text());
                                        outMessage.setId("2");

                                        messageArrayList.add(outMessage);


                                        // speak the message
                                        new SayTask().execute(outMessage.getMessage());
                                        break;

                                    case "option":
                                        outMessage = new Message();
                                        String title = r.title();
                                        String OptionsOutput = "";
                                        for (int i = 0; i < r.options().size(); i++) {
                                            DialogNodeOutputOptionsElement option = r.options().get(i);
                                            OptionsOutput = OptionsOutput + option.getLabel() + "\n";

                                        }
                                        outMessage.setMessage(title + "\n" + OptionsOutput);
                                        outMessage.setId("2");

                                        messageArrayList.add(outMessage);

                                        // speak the message
                                        new SayTask().execute(outMessage.getMessage());
                                        break;

                                    case "image":
                                        outMessage = new Message(r);
                                        messageArrayList.add(outMessage);

                                        // speak the description
                                        new SayTask().execute("You received an image: " + outMessage.getTitle() + outMessage.getDescription());
                                        break;
                                    default:
                                        Log.e("Error", "Unhandled message type");
                                }
                            }

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mAdapter.notifyDataSetChanged();


                                    if (mAdapter.getItemCount() > 1) {
                                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);

                                    }

                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }
    }



    private void showDialog(){

        dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.info_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button button = dialog.findViewById(R.id.anladim);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("clicked", false);
                editor.apply();
                dialog.dismiss();
            }
        });


        if (!answerClick){
            dialog.dismiss();

        }else{
            dialog.show();
        }


    }
    //Record a message via Watson Speech to Text


    /**
     * Check Internet Connection
     *
     * @return
     */
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions(InputStream audio) {
        return new RecognizeOptions.Builder()
                .audio(audio)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }











    private class SayTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
//            streamPlayer.playStream(textToSpeech.synthesize(new SynthesizeOptions.Builder()
//                    .text(params[0])
//                    .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
//                    .accept(HttpMediaType.AUDIO_WAV)
//                    .build()).execute().getResult());
            return "Did synthesize";
        }
    }


    @Override
    public void onBackPressed() {
        dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.info_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


            dialog.show();




    }




    private TeyitPosts CheckQuestionIsIncludedInTeyit(String message) {

        TeyitPosts isIncluded = null;

        int comp_result = 0;
        String[] eachWord = new String[0];
        Boolean IncludedAtTeyit = false;

        for (TeyitPosts post : listPosts) {
            String title = post.getTitle();


            if (title != null)
                if (title.contains(message)) {

                    isIncluded = post;


                }

        }


        return isIncluded;

    }

    private void ResponseByTeyit(TeyitPosts post) {

        String title = post.getTitle();
        String url = post.getUrl();
        String image_url = post.getImage_url();

        Message outMessage;
        outMessage = new Message();
        outMessage.setMessage(title);

        outMessage.setSourced(true);
        outMessage.setImage_url(image_url);

        outMessage.setSource_url(url);
        outMessage.setId("2");

        messageArrayList.add(outMessage);
        mAdapter.notifyDataSetChanged();


    }

    private void retrieveTeyit(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("cgx");
        query.getInBackground("ogPVvyP0Wx", new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null){

                    String title = object.get("title").toString();
                    String url = object.get("url").toString();
                    String image_url = object.get("image_url").toString();


                    listPosts.add(new TeyitPosts(title, url, image_url));



                }
            }
        });


    }


}



