package net.cmbt.talkbackdog;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cmbt.talkbackdog.R;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout mStartRecordBtn;
    private Button mStopRecordBtn;
    private Button mPlayAudioBtn;
    private Button mStopAudioBtn;
    private AudioUtil mAudioUtil;
    private static final int BUFFER_SIZE = 1024 * 2;
    private static byte[] mBuffer;
    private static File mAudioFile;
    private static ExecutorService mExecutorService;
    private static final String TAG = "MainActivity";
    private TextView ting;
    private TextView send;
    private ListView lv;
    int chat_index = 0;
    int revice_index = 0;
    private String local = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/local";
    private String message = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/message";
    private HashMap<String, File> datas;
    private ArrayList<DatasBean> datasBeans = new ArrayList<>();
    private MainActivity.myAdapter myAdapter;
    private File file_local;
    private File file_lmessage;
    private TextView ip_change;
    private AlertDialog show;
    private TextView ip_ssds;
    private LinearLayout show1;
    private LinearLayout show2;
    boolean TAG_T = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartRecordBtn = (RelativeLayout) findViewById(R.id.start_record_button);
        mStopRecordBtn = (Button) findViewById(R.id.stop_record_button);
        mPlayAudioBtn = (Button) findViewById(R.id.play_audio_button);
        mStopAudioBtn = (Button) findViewById(R.id.stop_audio_button);
        ting = (TextView) findViewById(R.id.ting);
        send = (TextView) findViewById(R.id.send);
        ip_change = (TextView) findViewById(R.id.ip_change);
        ip_ssds = (TextView) findViewById(R.id.ip_ssds);
        lv = (ListView) findViewById(R.id.lv);
        show1 = (LinearLayout) findViewById(R.id.show1);
        show2 = (LinearLayout) findViewById(R.id.show2);
        mBuffer = new byte[BUFFER_SIZE];

        file_local = new File(local);
        file_lmessage = new File(message);
        if (!file_local.exists()) {
            file_local.mkdir();
        }

        if (!file_lmessage.exists()) {
            file_lmessage.mkdir();
        }


        SharedPreferences userSettings = getSharedPreferences("ip_db", 0);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putString("ip", "192.168.199.159");
        editor.commit();


        getFileSize(file_local, file_lmessage);
        myAdapter = new myAdapter();
        lv.setAdapter(myAdapter);

        lv.setSelection(datasBeans.size() - 1);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (datasBeans.get(i).getType() == 1) {
                    mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/record/local/selfRecord_" + datasBeans.get(i).getOrder() + ".pcm");
                } else if (datasBeans.get(i).getType() == 2) {
                    mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/record/message/message_" + datasBeans.get(i).getOrder() + ".pcm");
                }

                playAudio(mAudioFile);

            }
        });


        ip_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View doialogx = View.inflate(MainActivity.this, R.layout.doialog, null);
                builder.setView(doialogx);
                show = builder.show();

                TextView sava = (TextView) doialogx.findViewById(R.id.sava);
                final EditText et_v = (EditText) doialogx.findViewById(R.id.et_v);
                Log.e("", "" + et_v.getText().toString());

                sava.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences userSettings = getSharedPreferences("ip_db", 0);
                        SharedPreferences.Editor editor = userSettings.edit();
                        editor.putString("ip", et_v.getText().toString().trim());
                        editor.commit();
                        show.dismiss();
                        SharedPreferences userSettingsxz = getSharedPreferences("ip_db", 0);
                        String name = userSettingsxz.getString("ip", "null");
                        ip_change.setText(name);
                        IP = name;
                    }
                });


            }
        });

        mStartRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                // TODO Auto-generated methodstub
                switch (event.getAction()) {

                    case KeyEvent.ACTION_UP: {

                        mAudioUtil.stopRecord();
                        mAudioUtil.convertWavFile();
                        TAG_T = false;
                        show1.setVisibility(View.INVISIBLE);
                        show2.setVisibility(View.INVISIBLE);
                    }
                    case KeyEvent.ACTION_DOWN: {
                        //松开事件发生后执行代码的区域
                    }
                    default:

                        break;
                }
                return false;
            }
        });


        ip_ssds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFileSize(file_local, file_lmessage);
                myAdapter.notifyDataSetChanged();
                lv.setSelection(datasBeans.size() - 1);
            }
        });


        mStartRecordBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("执行zh9ixing执行zh9ixing", "执行zh9ixing");

                TAG_T = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (TAG_T) {
                            try {
                                Thread.sleep(100);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (show1.getVisibility() == View.VISIBLE) {
                                            show1.setVisibility(View.INVISIBLE);
                                            show2.setVisibility(View.INVISIBLE);
                                        } else {
                                            show1.setVisibility(View.VISIBLE);
                                            show2.setVisibility(View.VISIBLE);
                                        }

                                    }
                                });


                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

                mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/record/local/selfRecord_" + chat_index + ".pcm");
                mAudioUtil = new AudioUtil(chat_index);
                mAudioUtil.startRecord();
                mAudioUtil.recordData();
                chat_index++;
                return false;
            }
        });

        ting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio(mAudioFile);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFileMessage();
                getFileSize(file_local, file_lmessage);
                myAdapter.notifyDataSetChanged();
                lv.setSelection(datasBeans.size() - 1);
            }
        });
        createFileServerSocket();//监听 收文件
    }


    class myAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datasBeans.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View item = null;

            if (datasBeans.get(i).getType() == 2) {
                item = View.inflate(viewGroup.getContext(), R.layout.item_left, null);
            } else if (datasBeans.get(i).getType() == 1) {
                item = View.inflate(viewGroup.getContext(), R.layout.item_right, null);
            }

            TextView time = (TextView) item.findViewById(R.id.time);
            TextView tie_vice = (TextView) item.findViewById(R.id.tie_vice);
            ImageView llls = (ImageView) item.findViewById(R.id.llls);
            time.setText(getDateTimeByMillisecond(datasBeans.get(i).getCreateTime()));

            long count = datasBeans.get(i).getFile().length() / 44100 / 4;
            if (count < 1)
                count = 1;
            tie_vice.setText(count + "s");

            if (count < 2)
                count = 2;

            if (count > 7)
                count = 7;
            ViewGroup.LayoutParams para = llls.getLayoutParams();
            para.width = 100 * (int) count + 100;
            llls.setLayoutParams(para);
            return item;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences userSettings = getSharedPreferences("ip_db", 0);
        String name = userSettings.getString("ip", "192.168.199.159");
        ip_change.setText(name);
        IP = name;
    }

    // 获得文件夹内文件的个数。
    public void getFileSize(File file_local, File file_lmessage) {
        datasBeans.clear();
        try {
            File[] flist1 = file_local.listFiles();
            File[] flist2 = file_lmessage.listFiles();

            for (int i = 0; i < flist1.length; i++) {
                DatasBean datasBean = new DatasBean();
                datasBean.setFile(flist1[i]);
                datasBean.setType(1);
                datasBean.setCreateTime(flist1[i].lastModified());
                datasBean.setOrder(i);
                datasBeans.add(datasBean);
            }

            for (int i = 0; i < flist2.length; i++) {
                DatasBean datasBean = new DatasBean();
                datasBean.setFile(flist2[i]);
                datasBean.setType(2);
                datasBean.setCreateTime(flist2[i].lastModified());
                datasBean.setOrder(i);
                datasBeans.add(datasBean);
            }


            Comparator<DatasBean> comparator = new Comparator<DatasBean>() {
                public int compare(DatasBean s1, DatasBean s2) {
                    //先排年龄
                    if (s1.getCreateTime() < s2.getCreateTime()) {
                        return (int) (s1.getCreateTime() - s2.getCreateTime());
                    }
                    return 0;
                }
            };


            Collections.sort(datasBeans, comparator);

            for (int i = 0; i < datasBeans.size(); i++) {
                Log.e("", "时间跑徐：" + getDateTimeByMillisecond(datasBeans.get(i).getCreateTime()));
            }


        } catch (Exception e) {

        }
    }


    private boolean delZSPic(String filePath) {
        boolean flag = true;
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                File[] filePaths = file.listFiles();
                for (File f : filePaths) {
                    if (f.isFile()) {
                        f.delete();
                    }
                    if (f.isDirectory()) {
                        String fpath = f.getPath();
                        delZSPic(fpath);
                        f.delete();
                    }
                }
            }
        } else {
            flag = false;
        }
        return flag;
    }

    private void initEvent() {
        mStartRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAudioUtil.startRecord();
                mAudioUtil.recordData();
            }
        });
        mStopRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAudioUtil.stopRecord();
                mAudioUtil.convertWavFile();
            }
        });

        mPlayAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudioFile != null) {
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            playAudio(mAudioFile);
                        }
                    });
                }
            }
        });

        mStopAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }


    private static void playAudio(File audioFile) {
        Log.d("MainActivity", "lu yin kaishi");
        int streamType = AudioManager.STREAM_MUSIC;
        int simpleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int mode = AudioTrack.MODE_STREAM;

        int minBufferSize = AudioTrack.getMinBufferSize(simpleRate, channelConfig, audioFormat);
        AudioTrack audioTrack = new AudioTrack(streamType, simpleRate, channelConfig, audioFormat,
                Math.max(minBufferSize, BUFFER_SIZE), mode);
        audioTrack.play();
        Log.d(TAG, minBufferSize + " is the min buffer size , " + BUFFER_SIZE + " is the read buffer size");

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(audioFile);
            int read;
            while ((read = inputStream.read(mBuffer)) > 0) {
                Log.d("MainActivity", "lu yin kaishi11111");

                audioTrack.write(mBuffer, 0, read);
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        delZSPic(local);
        delZSPic(message);
    }


    /**
     * 启动线程 向服务器发送文件
     */
    private void sendFileMessage() {
        new Thread(runx).start();
    }


    //发送 线程等
    String IP = "192.168.199.159";
    String fliePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/record";
    int PORT = 55239;
    Runnable runx = new Runnable() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(IP, PORT));
                //获取输出流
                OutputStream ou = socket.getOutputStream();
                //读取服务器响应
                BufferedReader bff = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                String line = null;
                String buffer = "";
                while ((line = bff.readLine()) != null) {
                    buffer = line + buffer;
                }
                //向服务器发送文件
                File file = new File(fliePath);
                if (file.exists()) {
                    FileInputStream fileInput = new FileInputStream(mAudioFile);
                    DataOutputStream dos = new DataOutputStream(ou);
                    // 文件名
                    dos.writeUTF(file.getName());
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    while ((length = fileInput.read(bytes)) != -1) {
                        dos.write(bytes, 0, length);
                    }
                    //告诉服务端，文件已传输完毕
                    socket.shutdownOutput();
                    fileInput.close();
                    dos.close();
                    //服务器返回码
                    Message message = new Message();
                    message.what = 2;
                    message.obj = buffer;
//                        handler.sendMessage(message);
                }
                //关闭各种输入输出流
                ou.flush();
                bff.close();
                ou.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    int FileProt = 55239;
    String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/record/message";

    /**
     * 创建服务端ServerSocket
     * 接收文件
     */
    private void createFileServerSocket() {
        new Thread(runs).start();


    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            getFileSize(file_local, file_lmessage);
            myAdapter.notifyDataSetChanged();
            lv.setSelection(datasBeans.size() - 1);
            return false;
        }
    });


    Runnable runs = new Runnable() {
        @Override
        public void run() {
            //给发送端返回一个消息，告诉他链接接收成功。
            String str = "200";
            try {
                ServerSocket serverSocket = new ServerSocket(FileProt);
                while (true) {
                    try {
                        //此处是线程阻塞的,所以需要在子线程中
                        Socket socket = serverSocket.accept();
                        //请求成功，响应客户端的请求
                        OutputStream out = socket.getOutputStream();
                        out.write(str.getBytes("utf-8"));
                        out.flush();
                        socket.shutdownOutput();
                        //获取输入流,读取客户端发送来的文件
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        // 文件名和长度
                        String fileName = dis.readUTF();
                        //接收到的文件要存储的位置
                        File directory = new File(sdPath);
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        FileOutputStream fos = new FileOutputStream(directory.getAbsolutePath() + "/message_" + revice_index + ".pcm");
                        // 开始接收文件
                        byte[] bytes = new byte[1024];
                        int length = 0;
                        while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                            fos.write(bytes, 0, length);
                            fos.flush();
                        }
                        revice_index++;
                        handler.sendEmptyMessage(1);

                        dis.close();
                        fos.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    };

    public String getDateTimeByMillisecond(long str) {
        Date date = new Date(str);
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm:ss");
        String time = format.format(date);
        return time;
    }

}
