package team.edu.app.muternet.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;

import team.edu.app.muternet.Activity.MainActivity;
import team.edu.app.muternet.R;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.net.Socket;
import java.util.UnknownFormatFlagsException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ClientFragment.OnClientFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ClientFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClientFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int SERVERPORT = 6668;
    private String SERVER_IP = "137.112.219.25";

    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor = Color.GREEN;
    private EditText edMessage;
    private OnClientFragmentInteractionListener mListener;
    private EditText IP_Addr;
    private EditText IP_Port;
    Button connectServer;
    Button sendData;

    private View view;

    public ClientFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClientFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClientFragment newInstance(String param1, String param2) {
        ClientFragment fragment = new ClientFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_client, container, false);
        handler = new Handler();
        msgList = view.findViewById(R.id.msgList);
        edMessage = view.findViewById(R.id.edMessage);
        IP_Addr = view.findViewById(R.id.IP_Addr);
        IP_Port = view.findViewById(R.id.IP_Port);
        connectServer = view.findViewById(R.id.connect_server);
        connectServer.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                msgList.removeAllViews();
                SERVERPORT = Integer.parseInt(IP_Port.getText().toString());
                SERVER_IP = IP_Addr.getText().toString();
                showMessage("Connecting to Server at." + SERVER_IP +": "+SERVERPORT, Color.WHITE);
                clientThread = new ClientThread();
                thread = new Thread(clientThread);
                thread.start();
                showMessage("Connected to Server...", clientTextColor);
                return;
            }
        });
        sendData = view.findViewById(R.id.send_data);
        sendData.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                String clientMessage = edMessage.getText().toString().trim();
                showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage);
                }
            }
        });
        return view;
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;
        private byte buffer[] = new byte[2048];
        private String message;

        @Override
        public void run() {


            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {
    /*
                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        showMessage(message, Color.RED);
                        break;
                    }*/
                    if (socket != null && socket.isConnected()) {
                        showMessage("Server Connected", clientTextColor);
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

                            //Handshake Process
                            showMessage("Starting handshaking process...",Color.YELLOW);
                            showMessage("Receiving secret handshaking code: \"Loading......\" ...",Color.YELLOW);
                            byte[] handshakeMsg = new byte[200];
                            try {
                                while(inputStream.available() < 1){}
                                inputStream.read(handshakeMsg);
                            }catch(Exception e){}

                            String secretMsg = new String(handshakeMsg).trim();
                            if(!secretMsg.equals("!@#$%^&*()_+")){
                            showMessage("Handshaking failed \"WTF who are you?\"",Color.YELLOW);
                                throw new UnknownFormatFlagsException("Handhsake failed");
                            }
                            showMessage("Handshaking success!\"doing good, feeling good~~~\"",Color.YELLOW);
                            showMessage("Transfering secret handshaking code...",Color.YELLOW);

                            try {
                                outputStream.write("!@#$%^&*()_+".getBytes());
                            }catch(Exception e){}
//                          showMessage(secretMsg,Color.YELLOW);
                            //Handshaking done


                        while (inputStream != null) {
                            if (inputStream.available() > 0) {
                                InputStream bufferedIn = new BufferedInputStream(inputStream);

                                File file = new File(Environment.getExternalStorageDirectory().toString() + "/sampleMusic.mp3");
                                FileOutputStream fos = new FileOutputStream(file);
                                BufferedOutputStream bufferedOut = new BufferedOutputStream(fos);


                                message = "Receieving File.";
                                showMessage(message, Color.BLUE);

                                int bytesRead;
                                while ((bytesRead= bufferedIn.read(buffer)) > 0)
                                {
                                    bufferedOut.write(buffer, 0, bytesRead);
                                }

                                fos.close();
                                message = "File Transfer Complete.";
                                showMessage(message, Color.BLUE);
                            }
                        }
                    }
                    socket.close();
                    String message = "Exiting...";
                    showMessage("Server: " + message, clientTextColor);

                    Thread.interrupted();
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if ( socket != null) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream(),"UTF-8")),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this.getContext());
        tv.setTextColor(color);
        tv.setText(message + " [" + Calendar.getInstance().getTime() + "]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message, color));
            }
        });
    }
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onClientFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClientFragmentInteractionListener) {
            mListener = (OnClientFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnClientFragmentInteractionListener {
        // TODO: Update argument type and name
        void onClientFragmentInteraction(Uri uri);
    }
}
