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
        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.fragment_container, PlayerFragment.newInstance());
        ft.commit();

        bottomNavigation = findViewById(R.id.buttom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.player:
                        fragment = PlayerFragment.getInstance();
                        break;
                    case R.id.server:
                        fragment = new ServerFragment();
                        break;
                    case R.id.client:
                        fragment = new ClientFragment();
                        break;
                }
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, fragment).commit();
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
