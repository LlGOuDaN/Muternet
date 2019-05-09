package team.edu.app.muternet.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.zip.Inflater;

import team.edu.app.muternet.Fragment.ClientFragment;
import team.edu.app.muternet.Fragment.PlayerFragment;
import team.edu.app.muternet.Fragment.ServerFragment;
import team.edu.app.muternet.R;

public class MainActivity extends AppCompatActivity implements ClientFragment.OnClientFragmentInteractionListener, ServerFragment.OnServerFragmentInteractionListener
{
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private PlayerFragment playerFragment;
    private ServerFragment serverFragment;
    private ClientFragment clientFragment;
    private Fragment fragment;
    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerFragment = new PlayerFragment();
        playerFragment.setArguments(new Bundle());
        serverFragment = new ServerFragment();
        serverFragment.setArguments(new Bundle());
        clientFragment = new ClientFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, clientFragment,"3").hide(clientFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, serverFragment, "2").hide(serverFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, playerFragment,"1").commit();
        fragment = playerFragment;

        bottomNavigation = findViewById(R.id.buttom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.player:
                        fragmentManager.beginTransaction().hide(fragment).show(playerFragment).commit();
                        fragment = playerFragment;
                        break;
                    case R.id.server:
                        fragmentManager.beginTransaction().hide(fragment).show(serverFragment).commit();
                        fragment = serverFragment;
                        break;
                    case R.id.client:
                        fragmentManager.beginTransaction().hide(fragment).show(clientFragment).commit();
                        fragment = clientFragment;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.load_file){
            Log.d("debug", "load file clicked");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(intent, 1);

            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode == RESULT_OK){
                uri = data.getData();
                Log.d("uri",uri.toString());
                Log.d(">>", "uri "+  getFileName(uri));
                Bundle bundle = new Bundle();
                bundle.putParcelable("musicURI", uri);
                bundle.putString("musicName", getFileName(uri));
                playerFragment.getArguments().putAll(bundle);
                serverFragment.getArguments().putAll(bundle);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Success!");
                alertDialog.setMessage("You've already successfully setup the media!\n" +
                        "Click Play Button to Play!");
                alertDialog.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClientFragmentInteraction(Uri uri) {

    }

    @Override
    public void onServerFragmentInteraction(Uri uri) {

    }

    //method from https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
