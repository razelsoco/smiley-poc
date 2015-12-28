package dbs.smileytown.poc.provider;

import android.nfc.tech.IsoDep;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

import java.io.IOException;

/**
 * Created by razelsoco on 16/12/15.
 */
public class Provider implements IProvider {
    private IsoDep mTagCom;
    @Override
    public byte[] transceive(byte[] pCommand) throws CommunicationException {

        byte[] response=null;
        try{
            response = mTagCom.transceive(pCommand);
        }catch(IOException e){
            throw new CommunicationException(e.getMessage());
        }
        return response;
    }

    public void setTagCom(IsoDep mTagCom) {
        this.mTagCom = mTagCom;
    }
}
