package com.example.jarvisapp;

import static com.example.ai_webza_tec.ai_method.checkForPreviousCallList;
import static com.example.ai_webza_tec.ai_method.clearContactListSavedData;
import static com.example.ai_webza_tec.ai_method.getContactList;
import static com.example.ai_webza_tec.ai_method.makeCall;
import static com.example.ai_webza_tec.ai_method.makeCallFromSavedContactList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SpeechRecognizer speechRecognizer;
    TextView tv_result;
    TextToSpeech textToSpeech;
    private FusedLocationProviderClient fusedLocationClient;
    private final String weatherapi = "USE_YOUR_OWN_API"; //Weather API Key
    private final String appid = "USE_YOUR_OWN_API";
    private final String aimodelapikey = "USE_YOUR_OWN_API"; //Ai model API Key
    private DecimalFormat df = new DecimalFormat("#.##");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });


        requestAudioPermission();

    }




    //Handling permission requests for audio recording, location access, contacts, and phone calling,
    //initializing necessary components upon granting permissions.
    private void requestAudioPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            init();
                            initiliazetextospeech();
                            Listening();
                        } else {
                            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            System.exit(0);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }



   // Initialization of text-to-speech engine with engine availability check and greeting message.
    private void initiliazetextospeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(textToSpeech.getEngines().size()==0){
                    Toast.makeText(MainActivity.this, "Engine Not Available", Toast.LENGTH_SHORT).show();
                }
                else{
                    String message = MyFunctions.wishing();
                    speak("Hey I am LOLO..." + " " + message);
                }
            }
        });
    }



   // Text-to-speech method for speaking messages with compatibility check.
    private void speak(String msg) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            textToSpeech.speak(msg,TextToSpeech.QUEUE_FLUSH,null,null);
        }
       else{
           textToSpeech.speak(msg,TextToSpeech.QUEUE_FLUSH,null);
        }
    }


    //Id of the TextView
    private void init(){
        tv_result=findViewById(R.id.tv_result);
    }




    //Sets up and manages speech recognition functionality in the app.
    private void Listening() {
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle bundle) {
                    ArrayList<String> Result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    Toast.makeText(MainActivity.this, Result.get(0), Toast.LENGTH_SHORT).show();
                    tv_result.setText(Result.get(0));
                    response(Result.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }





    //Handles various voice commands and actions, including weather updates,
    //alarm setting, calling contacts, and opening social media platforms and apps.
    private void response(String s) {
        String msg = s.toLowerCase();

        if (msg.contains("hey")) {
            speak("Hey there! How's it going?");
        } else if (msg.contains("i am fine")) {
            speak("That's awesome to hear!");
        } else if (msg.contains("tell me a joke")) {
            speak("Why don't scientists trust atoms? Because they make up everything!");
        } else if (msg.contains("how are you")) {
            speak("I'm just a bunch of code, but I'm here to help! Also, I don't get tired, so I'm always feeling 'byte'-er than ever!");
        } else if (msg.contains("what's the meaning of life")) {
            speak("To explore, learn, and enjoy the journey!");
        } else if (msg.contains("what's the time")) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            speak("It's currently " + time);
        } else if (msg.contains("what's the date")) {
            String date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
            speak("Today's date is " + date);
        } else if (msg.contains("weather")) {
            speak("Let me check the weather for you...");
            // You can integrate a weather API here
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            getLocationAndWeather();
                        }
                    }, 1800);

        } else if (msg.contains("open camera")) {
            speak("Opening the camera app...");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(intent);
        } else if (msg.contains("set alarm")) {
            speak("Setting an alarm for 5:30 AM...");
            if (msg.contains("set alarm")) {
                speak("Setting an alarm...");
                try {
                    Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                    intent.putExtra(AlarmClock.EXTRA_HOUR, 5);
                    intent.putExtra(AlarmClock.EXTRA_MINUTES, 30);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    speak("Sorry, I couldn't set the alarm. Please try manually.");
                }
            }
        } else if (msg.contains("open maps")) {
            speak("Opening Google Maps...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com"));
            startActivity(intent);
        } else if (msg.contains("send email")) {
            speak("Let's compose an email...");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Hello!");
            intent.putExtra(Intent.EXTRA_TEXT, "How are you?");
            startActivity(Intent.createChooser(intent, "Send Email"));
        } else if (msg.contains("play sidhu song")) {
            speak("OKay Sir, Check this:");
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            playSidhuVideo();
                        }

                        private void playSidhuVideo() {
                            String videoUrl = "https://youtube.com/shorts/fsDBk5ZMOus?si=vMPZBA0Mon40W6sP";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                            startActivity(intent);
                        }
                    }, 2000);
        } else if (msg.contains("jatt")) {
            speak("Jatt is a brand caste");
        } else if (msg.contains("call")) {
            final String[] listname = {""};
            final String name = MyFunctions.fetchname(msg);

            Dexter.withContext(this)
                    .withPermissions(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CALL_PHONE
                    ).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                if (checkForPreviousCallList(MainActivity.this)) {
                                    speak(makeCallFromSavedContactList(MainActivity.this, name));
                                } else {
                                    HashMap<String, String> list = getContactList(MainActivity.this, name);
                                    if (list.size() > 1) {
                                        for (String i : list.keySet()) {
                                            listname[0] = listname[0].concat("...........................!" + i);
                                        }
                                        speak("Which one sir ?.. There is " + listname[0]);
                                    } else if (list.size() == 1) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            makeCall(MainActivity.this, list.values().stream().findFirst().get());
                                            clearContactListSavedData(MainActivity.this);
                                        }
                                    } else {
                                        speak("No contact Found");
                                        clearContactListSavedData(MainActivity.this);
                                    }
                                }
                            }
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                // Handle the case where permissions are permanently denied
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        }
        else if (msg.contains("open youtube")) {
            speak("Opening YouTube...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
            startActivity(intent);
        } else if (msg.contains("open instagram")) {
            speak("Opening Instagram...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"));
            startActivity(intent);
        } else if (msg.contains("open facebook")) {
            speak("Opening Facebook...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com"));
            startActivity(intent);
        } else if (msg.contains("open telegram")) {
            speak("Opening Telegram...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org"));
            startActivity(intent);
        } else if (msg.contains("open messenger")) {
            speak("Opening Messenger...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com"));
            startActivity(intent);
        }else if (msg.contains("open twitter")) {
            speak("Opening Twitter...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com"));
            startActivity(intent);
        } else if (msg.contains("open linkedin")) {
            speak("Opening LinkedIn...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com"));
            startActivity(intent);
        } else if (msg.contains("open reddit")) {
            speak("Opening Reddit...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com"));
            startActivity(intent);
        } else if (msg.contains("open snapchat")) {
            speak("Opening Snapchat...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.snapchat.com"));
            startActivity(intent);
        } else if (msg.contains("open tiktok")) {
            speak("Opening TikTok...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com"));
            startActivity(intent);
        } else if (msg.contains("open pinterest")) {
            speak("Opening Pinterest...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pinterest.com"));
            startActivity(intent);
        } else if (msg.contains("open twitch")) {
            speak("Opening Twitch...");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitch.tv"));
            startActivity(intent);
        } else if (msg.contains("i love you")) {
            speak("I love youuuuuuuu tooooooooooooooooooo.......");
        } else{
            callModel(msg);
        }

//        else {
//            speak("I'm sorry, I didn't quite catch that!");
//        }
    }





    //Recording Button
    public void Recordingonclick(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 11);
        speechRecognizer.startListening(intent);
    }




    //After Finish it will shutdown the speech Stored
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }




    //Generate response from AI model based on input text.
    public void callModel(String promptText) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", aimodelapikey);

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(promptText).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    speak(resultText);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    speak("Sorry, I couldn't get a response from the model.");
                }
            }, this.getMainExecutor());
        }
    }




    //Retrieve user's location and fetch weather data if permission is granted.
    private void getLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    getWeather(location.getLatitude(), location.getLongitude());
                } else {
                    speak("I couldn't get your location. Please try again.");
                }
            }
        });
    }




    //Fetches weather data based on provided latitude and longitude using API.
    private void getWeather(double latitude, double longitude) {
        String tempUrl = weatherapi + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + appid;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    JSONArray weatherArray = jsonResponse.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    String description = weatherObject.getString("description");

                    JSONObject mainObject = jsonResponse.getJSONObject("main");
                    double temp = mainObject.getDouble("temp") - 273.15;
                    int tempInt = (int) Math.round(temp);

                    String cityName = jsonResponse.getString("name");

                    String result = "The current weather in " + cityName + " is " + description +
                            " with a temperature of " + tempInt + " degrees Celsius.";

                    speak(result);

                } catch (JSONException e) {
                    e.printStackTrace();
                    speak("I couldn't fetch the weather data.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                speak("There was an error fetching the weather data.");
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }



}