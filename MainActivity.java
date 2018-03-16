package com.example.cfgs.reproductor_canciones;

import android.content.Context;
import android.media.MediaPlayer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;
import android.widget.ToggleButton;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.color.white;

//import org.apache.commons.io.FilenameUtils;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mp;
    Button b5;
    int posicion = 0, pos_cancion = -1;
    private ArrayList<String> canciones = new ArrayList<String>();
    private ListView lv1;
    private String cancion_seleccionada;
    ToggleButton tb1, tb2;
    private Boolean circular = false, pausado = false, playing = false;
    private String directorioRaiz;
    private List listaNombresArchivos;
    private List<String> item = new ArrayList<String>();
    EditText inputSearch;
    ArrayAdapter<String> adapter;
    private ImageView play_pause, loop1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play_pause = (ImageView)findViewById(R.id.play);

        //tb1 = (ToggleButton)findViewById(R.id.loop1);
        //loop1 - toggle_button_background  en algún momento dejó de funcionar y no sé porqué
        //cambio a un toggle button mediante código

        loop1 = (ImageView)findViewById(R.id.loop);

        tb2 = (ToggleButton)findViewById(R.id.loop2);

        tb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    circular = true;  // The toggle is enabled
                    if (mp != null) mp.setLooping(true);
                } else {
                    circular = false;  // The toggle is disabled
                    if (mp != null) mp.setLooping(false);
                }
            }
        });


        //File f = new File("/mnt/shared/TarjetaSD/");
        //File f = new File("/sdcard/");
        File f = new File(Environment.getExternalStorageDirectory() + "/Music/");


        File[] files = f.listFiles();

        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];

            if (file.isFile()) {
                String name = file.getName();
                int pos = file.getName().lastIndexOf(".");
                if(pos != -1) {
                    name = file.getName().substring(0, pos);
                }
                //String fileNameWithOutExt = FilenameUtils.removeExtension(fileNameWithExt);  //apache library
                canciones.add(name);
            }
        }
        Collections.sort(canciones, String.CASE_INSENSITIVE_ORDER);

        lv1 =(ListView)findViewById(R.id.lv1);
        lv1.setTextFilterEnabled(true);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, canciones);
        lv1.setAdapter(adapter);

            lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                    cancion_seleccionada = lv1.getItemAtPosition(i).toString();
                    pos_cancion = canciones.indexOf(cancion_seleccionada);

                    lv1.setSelector(R.color.colorAccent);   //#b7f4ee  colors.xml

                    destruir();  //destruir al cambiar de canción
                    playing = false;
                    pausado = false;
                    iniciar_o_pausar(null);
                }
            });

                /* Activando el filtro de busqueda */

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub

                MainActivity.this.adapter.getFilter().filter(arg0);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

            }
        });

// Cerrar teclado

        lv1.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
    }


    public void destruir() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    /*tengo solo 3 botones, play, stop y pause */
    //si pausado == true then continuar, sino iniciar
    //añadir un botón tipo toggle para reproducir en modo bucle
    //http://stackoverflow.com/questions/18598255/android-create-a-toggle-button-with-image-and-no-text
    private void mostrar_mensaje() {
        Toast.makeText(this, "Playing End", Toast.LENGTH_SHORT).show();
    }

    public void iniciar_o_pausar(View v) {
        if (pos_cancion > 0) setPositionLV();
        if (!playing) {
            play_pause.setImageResource(R.drawable.pause);
            playing = true;
            if (pausado) {
                if (mp != null && mp.isPlaying() == false) {
                    mp.seekTo(posicion);
                    mp.start();
                    pausado = false;
                }
            } else {
                destruir();
                //int i = lv1.getCheckedItemPosition();
                //cancion_seleccionada=canciones.get(i);

                Uri song = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Music/" + cancion_seleccionada+".mp3");
                //Uri song = Uri.parse("/mnt/shared/TarjetaSD/" + cancion_seleccionada);
                mp = MediaPlayer.create(this, song);
                /*
                String path = Environment.getExternalStorageDirectory().getPath() + "/Music/" + cancion_seleccionada+".mp3";
                File f = new File(path);
                try {
                   AudioFile af = AudioFileIO.read(f);
                    Tag tag = af.getTag();

                    tag.deleteField(FieldKey.LYRICS);
                    tag.setField(FieldKey.LYRICS, "lyrics");

                    AudioHeader audioHeader = af.getAudioHeader();
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    String album = tag.getFirst(FieldKey.ALBUM);
                    String title = tag.getFirst(FieldKey.TITLE);

                    Integer seconds = af.getAudioHeader().getTrackLength();
                    tag.getFirst(FieldKey.ARTIST);
                    tag.getFirst(FieldKey.ALBUM);
                    tag.getFirst(FieldKey.TITLE);
                    tag.getFirst(FieldKey.COMMENT);
                    tag.getFirst(FieldKey.YEAR);
                    tag.getFirst(FieldKey.TRACK);
                    tag.getFirst(FieldKey.DISC_NO);
                    tag.getFirst(FieldKey.COMPOSER);
                    tag.getFirst(FieldKey.ARTIST_SORT);
                    TagField binaryField = tag.getFirstField(FieldKey.COVER_ART);
                    Artwork art = tag.getFirstArtwork();
                    art.getBinaryData();
                    Bitmap image= BitmapFactory.decodeByteArray(art.getBinaryData(),0,art.getBinaryData().length);
                    //im1.setImageBitmap(image);

                    Uri sArtworkUri = Uri.parse(path);
                    if (sArtworkUri !=null){
                        MediaMetadataRetriever mData=new MediaMetadataRetriever();
                        mData.setDataSource(this,sArtworkUri);
                        artist = mData.extractMetadata(METADATA_KEY_ARTIST);
                        title = mData.extractMetadata(METADATA_KEY_TITLE);
                        album = mData.extractMetadata(METADATA_KEY_ALBUM);

                        byte artwork[]=mData.getEmbeddedPicture();
                        image= BitmapFactory.decodeByteArray(artwork,0,artwork.length);
                        im1.setImageBitmap(image);  77carátula album
                    }

                } catch (CannotReadException | InvalidAudioFrameException | TagException | IOException | ReadOnlyFileException e) {
                    e.printStackTrace();
                }  */
                //mp = MediaPlayer.create(this, getResources().getIdentifier(cancion_seleccionada, "raw", getPackageName()));
                if (circular)
                    mp.setLooping(true);
                else {
                    mp.setLooping(false);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            //mostrar_mensaje();
                            nextSong(null);
                        }
                    });
                }
                mp.start();
            }
        }
        else {
            play_pause.setImageResource(R.drawable.play);
            playing = false;
            if (mp != null && mp.isPlaying()) {
                posicion = mp.getCurrentPosition();
                mp.pause();
                pausado = true;
            }
        }
    }

    public void nextSong(View view) {

        destruir();
        //int i = lv1.getCheckedItemPosition();
        pos_cancion++;
        if (pos_cancion == canciones.size()) pos_cancion = 0;
        playSong();
    }

    public void previousSong(View view) {

        destruir();
        //int i = lv1.getCheckedItemPosition();
        pos_cancion--;
        if (pos_cancion < 0 ) pos_cancion = canciones.size() - 1;
        playSong();
    }

    private void playSong() {
        cancion_seleccionada = canciones.get(pos_cancion);
        setPositionLV();

        Uri song = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Music/" + cancion_seleccionada+".mp3");
        //Uri song = Uri.parse("/mnt/shared/TarjetaSD/" + cancion_seleccionada);
        mp = MediaPlayer.create(this, song);

        mp.start();
        play_pause.setImageResource(R.drawable.pause);
        playing = true;
        pausado = false;
        if (!circular) {
            mp.setLooping(false);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //mostrar_mensaje();
                    nextSong(null);
                }
            });
        }
        else mp.setLooping(true);
    }

    private void setPositionLV()
    {
        //adapter.notifyDataSetChanged();
        //lv1.clearFocus();
        //lv1.setSelector(white);

        //lv1.getChildAt(pos_cancion).requestFocus();
        lv1.requestFocusFromTouch();
        for (int i = 0; i < canciones.size(); i++) {
            lv1.setItemChecked(i, false);
        }
        lv1.clearChoices();
        lv1.requestLayout();
        adapter.notifyDataSetChanged();
        //lv1.clearChoices();
        lv1.setSelection(pos_cancion);
        //lv1.setItemChecked(pos_cancion, true);
        //lv1.clearFocus();
        lv1.requestFocus();
        adapter.notifyDataSetChanged();
        //lv1.setSelector(R.color.colorAccent);
        //lv1.smoothScrollToPosition(pos_cancion);
/*
        lv1.clearFocus();
        lv1.setSelector(white);

        //lv1.setSelection(pos_cancion);
        lv1.post(new Runnable() {
            @Override
            public void run() {
                lv1.requestFocusFromTouch();
                lv1.getChildAt(pos_cancion).requestFocus();
                //lv1.setItemChecked(pos_cancion, true);
                lv1.setSelection(pos_cancion);
                lv1.requestFocus();
            }
        });
        lv1.setSelector(R.color.colorAccent);
        //adapter.notifyDataSetChanged();
*/
    }



    public void pausar(View v) {
        if (mp != null && mp.isPlaying()) {
            posicion = mp.getCurrentPosition();
            mp.pause();
            pausado = true;
        }
    }


    public void detener(View v) {
        if (pos_cancion > 0) setPositionLV();
        if (mp != null) {
            mp.stop();
            posicion = 0;
            pausado = false;
        }
        play_pause.setImageResource(R.drawable.play);
        playing = false;
    }

    public void set_modeLoop(View v) {
        if (circular) {
            loop1.setImageResource(R.drawable.bucle_no);
            circular = false;
            if (mp != null) mp.setLooping(false);
        }
        else {
            loop1.setImageResource(R.drawable.bucle_si);
            circular = true;
            if (mp != null) mp.setLooping(true);
        }
    }

}
