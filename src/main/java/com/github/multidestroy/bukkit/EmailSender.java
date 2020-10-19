package com.github.multidestroy.bukkit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Deprecated
public class EmailSender {

    private String to;
    private String from;
    private String host;
    private Properties properties;
    private Session session;

    EmailSender() {
      /*  Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String myAccountEmail = "pprochot16@gmail.com";
        String password = "logowanie";

        Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(myAccountEmail, password);
                    }
                }
        );

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(myAccountEmail));
            message.setSubject("My first email from javamail");
            message.setText("Hi there, \n it's my first e-mail");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        System.out.println("Wyslano e-maila!");*/
    }

    public boolean sendEmail() {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("This is the subject Line!");
            message.setContent("<h1>This is actual message</h1>", "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return false;
    }
}
