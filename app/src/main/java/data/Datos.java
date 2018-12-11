package data;

/**
 * DATOS = REF SP FAB SP UDS; Estos son los datos de protocolo
 * ; que define una pieza por su referencia <REF>
 * ; fabricante <FAB> y cantidad solicitada <UDS>
 * REF = 10*20DIGIT
 * FAB = 10*20DIGIT
 * UDS = 1*5DIGIT
 * */
public class Datos implements MyProtocol{
    protected String ref;
    protected String facturer;
    protected long uds;


    public Datos(String r, String f, int u) throws ProtocolError {
        this.ref=r;
        if(ref.length()<10 || ref.length()>20)
            throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

        this.facturer=f;
        this.uds=u;


    }
    /**
     *
     * @param input
     */
    public Datos(String input) throws ProtocolError {

        String[] parts = input.split(SP);
        if (parts.length == 3) {
            ref = parts[0];
            if(ref.length()<10 || ref.length()>20)
                throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

            facturer = parts [1];
            try {
                uds = Long.parseLong(parts[2]);
            }catch (NumberFormatException nex){
                throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);
            }
        } else throw new ProtocolError(ProtocolError.PROTOCOL_MALFORMED_RREQUEST);

    }

    public String toString(){
        String result="";

        result = ref + SP + facturer + SP +String.valueOf(uds);

        return result;
    }

    public String toXMLString() {

        return "";
    }
}
