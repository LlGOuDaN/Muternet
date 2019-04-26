package team.edu.app.muternet.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import team.edu.app.muternet.Fragment.ClientFragment;
import team.edu.app.muternet.Fragment.PlayerFragment;
import team.edu.app.muternet.Fragment.ServerFragment;
import team.edu.app.muternet.R;

public class MainActivity extends AppCompatActivity implements ClientFragment.OnClientFragmentInteractionListener, ServerFragment.OnServerFragmentInteractionListener
{
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PlayerFragment playerFragment = new PlayerFragment();
        final ServerFragment serverFragment = new ServerFragment();
        final ClientFragment clientFragment = new ClientFragment();

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
    public void onClientFragmentInteraction(Uri uri) {

    }

    @Override
    public void onServerFragmentInteraction(Uri uri) {

    }
}
