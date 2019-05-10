package team.edu.app.muternet.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.*;

import team.edu.app.muternet.DBConstants;
import team.edu.app.muternet.R;
import team.edu.app.muternet.Utils;
import team.edu.app.muternet.model.Group;

public class ServerFragment extends Fragment {
    CollectionReference groupRef = FirebaseFirestore.getInstance().collection(DBConstants.GROUP_COLLECTION);

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
    private Switch aSwitch;


    Button startServer = null;
    Button sendData = null;

    private View view;

    public ServerFragment() {
        // Required empty public constructor
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
        aSwitch = view.findViewById(R.id.on_switch);
//        final List<String> group_names = new ArrayList<>();
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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
//                                group_names.add(groupName);
                                Log.d("Firebase", "Group ID  new");
                            }
                        }
                    });
                    msgList.removeAllViews();

                    showMessage("Server Started. ", Color.WHITE);
                    showMessage("Server ip: " + getIPAddress(true) + ": " + SERVER_PORT, Color.GREEN);
                    serverThread = new Thread(new ServerThread());
                    serverThread.start();
                } else {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    groupRef.document(groupName).delete();
                }
            }


        });
        sendData = view.findViewById(R.id.send_data);
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

    /*
        Server Setup Thread
    */
    class ServerThread implements Runnable {

        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                clientSocket = serverSocket.accept();
                sendFile();
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
        }
    }

    /*
        File Transfer Thread
    */
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
            if (getContext() == null) {
                Log.d("ERR", "Context Null");
            } else {
                Log.d("ERR", getContext().toString());
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

            //Handshake Process
            showMessage("Starting handshaking process...", Color.YELLOW);
            showMessage("Sending secret handshaking code: \"whats up dude?\" ...", Color.YELLOW);
            File soundFile = new File(Utils.getRealPathFromURI(getContext(), uri));
            String header = String.format("FILE: %s", Utils.getFileName(getContext(), uri));
            header = soundFile.length() + ":" + header;
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
                throw new UnknownFormatFlagsException("Recv Name failed");
            }

            showMessage("Handshaking success!", Color.YELLOW);
            Log.d("uriPath", uri.toString());
            if ((Utils.getRealPathFromURI(getContext(), uri)).isEmpty()) {
                Log.d("uriPath", "Is Null");
            }

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

    class playThread implements Runnable {
        OutputStream outputStream;

        @Override
        public void run() {
            if (getContext() == null) {
                Log.d("ERR", "Context Null");
            } else {
                Log.d("ERR", getContext().toString());
            }
            sendPlayCommand();
        }

        private void sendPlayCommand() {
            try {
                outputStream = clientSocket.getOutputStream();
                outputStream.write("play".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class pauseThread implements Runnable {
        OutputStream outputStream;

        @Override
        public void run() {
            if (getContext() == null) {
                Log.d("ERR", "Context Null");
            } else {
                Log.d("ERR", getContext().toString());
            }
            sendPauseCommand();
        }

        private void sendPauseCommand() {
            try {
                outputStream = clientSocket.getOutputStream();
                outputStream.write("pause".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupRef.document(groupName).delete();
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

    //Listener-Methods and thread opeartions

    public void sendFile() {
        FileTransferThread fileThread = new FileTransferThread(clientSocket);
        new Thread(fileThread).start();
    }

    public void onPlayerPause() {
        pauseThread pauseThread = new pauseThread();
        new Thread(pauseThread).start();
    }

    public void onPlayerPlay() {
        playThread playThread = new playThread();
        new Thread(playThread).start();
    }

    public void onPlayerDrag() {

    }

    //Utils
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

}
