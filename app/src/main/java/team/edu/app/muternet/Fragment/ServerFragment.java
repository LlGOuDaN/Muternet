package team.edu.app.muternet.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Enumeration;

import team.edu.app.muternet.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ServerFragment.OnServerFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ServerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 9999;
    private LinearLayout msgList;
    private Handler handler;
    private int greenColor = Color.GREEN;
    private EditText edMessage;

    Button startServer = null;
    Button sendData = null;

    private OnServerFragmentInteractionListener mListener;
    private TextView textView;
    private View view;

    public ServerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ServerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ServerFragment newInstance(String param1, String param2) {
        ServerFragment fragment = new ServerFragment();
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
        getActivity().setTitle("Server");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_server, container, false);
        handler = new Handler();
        msgList = view.findViewById(R.id.msgList);
        startServer = (Button)view.findViewById(R.id.start_server);
        startServer.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                msgList.removeAllViews();
                showMessage("Server Started. " , Color.WHITE);
                serverThread = new Thread(new ServerThread());
                serverThread.start();
                return;
            }
        });
        sendData = (Button)view.findViewById(R.id.send_data);
        sendData.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                String msg = edMessage.getText().toString().trim();
                showMessage("Server : " + msg, Color.BLUE);
                sendMessage(msg);
            }
        });
        edMessage = view.findViewById(R.id.edMessage);
        return view;
    }
    private void sendMessage(final String message) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this.getContext());
        tv.setTextColor(color);
        tv.setText(message + " [" + Calendar.getInstance().getTime() +"]");
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

    class ServerThread implements  Runnable{

        @Override
        public void run() {
            Socket socket;
            try{


// and now you can pass it to your socket-constructor
                ServerSocket server = new ServerSocket(SERVER_PORT);

                view.findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
            if (serverSocket != null){
                while (!Thread.currentThread().isInterrupted()){
                    try {
                        socket = serverSocket.accept();
                        showMessage(socket.getRemoteSocketAddress().toString(), Color.WHITE);
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    }catch (IOException e){
                        e.printStackTrace();
                        showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable{
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket){
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try{
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            }catch (IOException e){
                e.printStackTrace();
                showMessage("Error Connecting to Client!!", Color.RED);
            }showMessage("Connected to Client!!", greenColor)
            ;
        }
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        showMessage("Client : " + read, greenColor);
                        break;
                    }
                    showMessage("Client : " + read, greenColor);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onServerFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnServerFragmentInteractionListener) {
            mListener = (OnServerFragmentInteractionListener) context;
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
    public interface OnServerFragmentInteractionListener {
        // TODO: Update argument type and name
        void onServerFragmentInteraction(Uri uri);
    }
}
