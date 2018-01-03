package com.xfsi.batterychecker.app;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
//import javax.mail.util.ByteArrayDataSource;

/**
 * Created by local-kieu on 6/25/14.
 */
// javax.mail.Authenticator is in one of the libs/jar
public class GMailSender extends Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;
    private String port = "465";

    private Context mContext;

    // I am not sure what does this do ???
    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(Context context, String user, String password) {
        this.mContext = context;
        this.user = user;
        this.password = password;

        // javax.util.Properties class
        Properties props = new Properties();
        // still little confuse between when should I use setProperty() and put()
        // they seems interchangeable

        // setProperty() is Properties method: Maps the specified key to the specified value
        // and the value can be a variable
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);

        // put() is Hashtable method that Properties extends, it only takes String key and value
        props.put("mail.smtp.auth", "true");

        //props.put("mail.smtp.port", "25");
        props.put("mail.smtp.port", port);

        //props.put("mail.smtp.socketFactory.port", "25");
        props.put("mail.smtp.socketFactory.port", port);

        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        /* Session is a mail session, it collects properties and defaults used by mail API.
			A def session object can be shared by multiple apps on the desktop.
		*/
        // getDefaultInstance(Properties props, Authenticator authenticator)
        session = Session.getDefaultInstance(props, this);
    }

    // PasswordAuthentication class holds String username, password, it is used by Authenticator
    // no Exception
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        MimeMessage message = new MimeMessage(session); // default constructor

        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));

        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);

        if (recipients.indexOf(',') > 0)
            // more than one recipients
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            // only one recipient
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

        // static send(Message msg)	Send a message.
       // Transport.send(message);
        Transport transport = session.getTransport("smtp");
        /*
        Transport.connect(host,user,password)
        throws:
            AuthenticationFailedException - for authentication failures
            MessagingException - for other failures
            IllegalStateException - if the service is already connected
         */
        // I got AuthenticationFailedException, kieu.hua@gmail.cm

        try {
            transport.connect(mailhost, 465, user, password);
        } catch (AuthenticationFailedException e) {
            BatteryAppWidget.errorNotify(mContext, "Sender email or password is invalid", 1);
        }

        /* send(Message, Address[])
        throws
        SendFailedException -if the message could not be sent to some or any of the recipients.
        MessagingException
        K I don't get this errorNotify, but I got undeliveried mail in my gmail account
        probablly it makes senses, but I leave this anyway.
        */
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } catch (SendFailedException e) {
            BatteryAppWidget.errorNotify(mContext, "Some recipients are invalid.", 2);
        }
        transport.close();
    }

    /*
	DataSource interface provides the JavaBeans Activation framework, it provides data type and access to it
		JavaBeans are reusable software components for Java, it encapsuates many objects into a single object.
	*/
    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }
        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type){
            this.type = type;
        }

        // From DataSource interface
        public String getContentType(){
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
           throw new IOException("Not Supported");
        }

        @Override
        public String getName() {
            return "ByteArrayDataSource";
        }
    }
}
