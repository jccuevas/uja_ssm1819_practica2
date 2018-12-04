package data;

import java.util.Date;

public class UserData {

    private String userName="";
    private String password="";
    private String domain="";
    private short port=0;
    private String sid="";
    private String expires;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    /**
     * Constructor por defecto con los valores de labtelema.ujaen.es:80
     */
    public UserData(){
        userName="user";
        password="12345";
        domain="labtelema.ujaen.es";
        port=80;
    }

    /**
     * constructor con parámetros
     * @param user nombre de ususario
     * @param pass clave
     * @param domain dominio o ip del servidor
     * @param port puerto del servidor
     */
    public UserData(String user,String pass,String domain,short port){
        this.userName=user;
        this.password=pass;
        this.domain=domain;
        this.port=port;
    }

    public String getUserName() {
        return userName;
    }

    public String getDomain() {
        return domain;
    }

    public short getPort() {
        return port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }
      /**
     *
     * @param input the data of the current user
     * @param session string with format SESSION-ID=xxxxx
     * @param expires string with forma EXPIRES=xxxx
     * @return updated user data
     */

      public static UserData processSession(UserData input,String session,String expires){
        session = session.substring(session.indexOf("=")+1,session.length());
        expires = expires.substring(expires.indexOf("=")+1,expires.length());
        input.setSid(session);

        input.setExpires(expires);

        return input;
    }
}
