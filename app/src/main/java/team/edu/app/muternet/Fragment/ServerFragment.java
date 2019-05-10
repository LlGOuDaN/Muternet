package team.edu.app.muternet.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.*;

import team.edu.app.muternet.DBConstants;
import team.edu.app.muternet.R;
import team.edu.app.muternet.Utils;
import team.edu.app.muternet.model.Group;

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
    CollectionReference groupRef = FirebaseFirestore.getInstance().collection(DBConstants.GROUP_COLLECTION);

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    private Socket clientSocket;
    Thread serverThread = null;
    private int SERVER_PORT = 9997;
    private String groupName;
    private LinearLayout msgList;
    private Handler handler;
    private int greenColor = Color.GREEN;
    private EditText edMessage;
    private EditText IP_Port;
    private EditText Group_ID;


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
        IP_Port = view.findViewById(R.id.IP_Port);
        Group_ID = view.findViewById(R.id.Group_Id);
        startServer = (Button) view.findViewById(R.id.start_server);
        startServer.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                groupName = Group_ID.getText().toString();
                SERVER_PORT = Integer.parseInt(IP_Port.getText().toString());
                groupRef.document(groupName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            Log.d("Firebase", "Group ID exits");
                        } else {
                            Group group = new Group(getIPAddress(true), SERVER_PORT);
                            groupRef.document(groupName).set(group);
                            Log.d("Firebase", "Group ID  new");
                        }
                    }
                });
                msgList.removeAllViews();

                showMessage("Server Started. ", Color.WHITE);
                showMessage("Server ip: " + getIPAddress(true) + ": " + SERVER_PORT, Color.GREEN);
                serverThread = new Thread(new ServerThread());
                serverThread.start();
                return;
            }
        });
        sendData = (Button) view.findViewById(R.id.send_data);
        sendData.setOnClickListener(new Button.OnClickListener() {

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
    //get the current IP Address
    //return: IP Address

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
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


    //create a text view with colored message

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

    class ServerThread implements Runnable {

        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                clientSocket = serverSocket.accept();
                sendFile(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
        }
    }
    class FileTransferThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private byte buffer[];
        private int count;
        private OutputStream outputStream;
        private InputStream inputStream;

        public FileTransferThread(Socket clientSocket) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            this.clientSocket = clientSocket;
            showMessage("Transfering File.", greenColor);
            if (clientSocket.isBound()) {
                try {
                    outputStream = clientSocket.getOutputStream();
                    inputStream = clientSocket.getInputStream();
                } catch (Exception e) {
                    showMessage("Client Socket not bound", Color.RED);
                }
            }
        }

        @Override
        public void run() {
            if (getContext() == null)
            {
                Log.d("ERR","Context Null");
            }else{
                Log.d("ERR",getContext().toString());
            }

            sendLoadedFile();
        }

        void sendLoadedFile() {
            Uri uri;
            if (getArguments() == null || (uri = getArguments().getParcelable("musicURI")) == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setMessage("No File is playing, cannot send file");
                alertDialog.setTitle("Error");
                alertDialog.show();
                return;
            }
            Log.d("uriPath", uri.getPath());
//            Log.d("uriPath", Utils.getRealPathFromURI(context, uri));
//                Log.d("uriPath", Environment.getExternalStorageDirectory().toString());
//                File soundFile = new File(Utils.getRealPathFromURI(getContext(), uri));

            //Handshake Process
            showMessage("Starting handshaking process...", Color.YELLOW);
            showMessage("Sending secret handshaking code: \"whats up dude?\" ...", Color.YELLOW);
            File soundFile = new File(Utils.getRealPathFromURI(getContext(), uri));
            String header = String.format("FILE: %s", Utils.getFileName(getContext(), uri));
            header = soundFile.length()+":"+header;
            try {
                outputStream.write(header.getBytes());
            } catch (Exception e) {
            }

            byte[] clientResponse = new byte[200];
            try {
                while (inputStream.available() < 0) {
                }
                inputStream.read(clientResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String secretMsg = new String(clientResponse).trim();
            showMessage(secretMsg, Color.YELLOW);
            if (!secretMsg.equals("FILE RECV")) {
                showMessage("Incorrect Message", Color.YELLOW);
                Log.d("RECV", secretMsg);
//                        throw new UnknownFormatFlagsException("Recv Name failed");
            }
            showMessage("Handshaking success!", Color.YELLOW);
            Log.d("uriPath",uri.toString());
            if ((Utils.getRealPathFromURI(getContext(), uri)).isEmpty()){
                Log.d("uriPath","Is Null");
//                Log.d("uriPath", )
            }

            showMessage("File: " + soundFile, greenColor);
//                soundFile = new File(Environment.getExternalStorageDirectory().toString() + "/Music/music.mp3");
            showMessage("File: " + soundFile, greenColor);
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(soundFile);
            } catch (Exception e) {
                fileInputStream = null;
                e.printStackTrace();
                showMessage("File Not found", Color.RED);
            }
            buffer = new byte[(int) soundFile.length()];

            try {
                while ((count = fileInputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, count);
                outputStream.flush();
            } catch (Exception e) {
                showMessage("Transfer Error", Color.RED);
            }
            showMessage("Done.", Color.WHITE);

            try {
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void sendFile(Socket socket){
        FileTransferThread fileThread = new FileTransferThread(socket);
        new Thread(fileThread).start();
    }



    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Connecting to Client!!", Color.RED);
            }
            showMessage("Connected to Client!!", greenColor)
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

//    private String getIpAddress() {
//        String ip = "";
//        try {
//            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
//                    .getNetworkInterfaces();
//            while (enumNetworkInterfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = enumNetworkInterfaces
//                        .nextElement();
//                Enumeration<InetAddress> enumInetAddress = networkInterface
//                        .getInetAddresses();
//                while (enumInetAddress.hasMoreElements()) {
//                    InetAddress inetAddress = enumInetAddress.nextElement();
//
//                    if (inetAddress.isSiteLocalAddress()) {
//                        ip += "SiteLocalAddress: "
//                                + inetAddress.getHostAddress() + "\n";
//                    }
//
//                }
//
//            }
//
//        } catch (SocketException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            ip += "Something Wrong! " + e.toString() + "\n";
//        }
//
//        return ip;
//    }

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
        try {
            if (serverSocket != null) {
                serverSocket.close();
                groupRef.document(groupName).delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupRef.document(groupName).delete();
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
