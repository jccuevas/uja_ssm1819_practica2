package data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PETICION = USER SP DATE SP 1*DATOS
 * <p>
 * USER = 6*20ALPHA; nombre
 * <p>
 * DATE = YYYY “-“ MM “-“ DD “-“ HH “-“ m “-“ SS
 * YYYY = 4DIGIT; Año expresado en cuatro dígitos
 * MM = 2DIGT; mes del año con 0. Ejemplo Enero el 01.
 * <p>
 * DATOS = REF SP FAB SP UDS; Estos son los datos de protocolo
 * ; que define una pieza por su referencia <REF>
 * ; fabricante <FAB> y cantidad solicitada <UDS>
 * REF = 10*20DIGIT
 * FAB = 10*20DIGIT
 * UDS = 1*5DIGIT
 */
public class Peticion implements MyProtocol {

    String user;
    String date;
    List<Datos> datos;

    /**
     * @param u
     */
    public Peticion(String u, Date date, List<Datos> list) throws ProtocolError {
        user = u;

        if (u.length() < 4 || u.length() > 20)
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("y-M-d-H-m-s");
            this.date = sdf.format(date);
        } catch (RuntimeException rex) {
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

        }
        if (list != null && list.size() >= 1) {
            datos = list;
        } else
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);


    }

    /**
     * @param u
     */
    public Peticion(String u, List<Datos> list) throws ProtocolError {
        user = u;

        if (u.length() < 4|| u.length() > 20)
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

        try {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("y-M-d-H-m-s");
            this.date = sdf.format(date);
        } catch (RuntimeException rex) {
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

        }
        if (list != null && list.size() >= 1) {
            datos = list;
        } else
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

    }


    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(user);
        sb.append(SP);
        sb.append(date);
        sb.append(SP);
        for (Datos dato : datos) {
            sb.append(dato.toString());
        }

        return sb.toString();
    }

    /**
     * <request user=xxx date=ddd>
     *    <part id=xxx fab=zzz uds=yyy/>
     *  </request>
     * @return
     */
    public String toXMLString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<request user=");
        sb.append(user);
        sb.append(" date=");
         sb.append(date);
        sb.append(">");
        for (Datos dato : datos) {
            sb.append(dato.toXMLString());
        }
        sb.append("</request>");
        return sb.toString();
    }
}
