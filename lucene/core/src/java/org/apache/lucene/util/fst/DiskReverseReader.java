/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.util.fst;
import java.io.IOException;
import org.apache.lucene.store.IndexInput;

final public class DiskReverseReader extends FST.BytesReader {

    final IndexInput indexInput;
    final long arcsBaseFilePointer;
    final long numBytes;
    private long curPos;
    
    public DiskReverseReader(IndexInput indexInput,long arcsBaseFilePointer,long numBytes)
    {
        this.indexInput = indexInput;
        this.arcsBaseFilePointer = arcsBaseFilePointer; 
        this.numBytes = numBytes;
    }
    
    // Note: should return the real position in IndexInput,because arc's node is the
    //       position in IndexInput!
    @Override
    public long getPosition() {
        return curPos;
    }

    @Override
    public void setPosition(long pos) {
        assert pos >= 0L;
        curPos = pos;
    }

    @Override
    public boolean reversed() {
        return true;
    }

    @Override
    public byte readByte() throws IOException {

        long realPos = curPos + arcsBaseFilePointer;
        if(realPos < 0)
        {
            throw new IOException(
                    "out of range,"+
                    "base:"+this.arcsBaseFilePointer+","+
                    "num:"+this.numBytes+","+
                    "cur:"+this.curPos + "," +
                    "real => "+realPos);
            
        }
        indexInput.seek(realPos);
        byte b = indexInput.readByte();
        curPos --;
        return b;
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        long realPos = curPos + arcsBaseFilePointer - (len - 1);
        if(realPos < 0)
        {
            throw new IOException(
                    "out of range,"+
                    "base:"+this.arcsBaseFilePointer+","+
                    "num:"+this.numBytes+","+
                    "cur:"+this.curPos + "," +
                    "real => "+realPos);
            
        }   
        indexInput.seek(realPos);
        indexInput.readBytes(b, offset, len);
        int down = 0,up = len - 1;
        while(down < up)
        {
            byte temp = b[offset + down];
            b[offset + down] = b[offset + up]; 
            b[offset + up] = temp;
            down++;
            up--;
        }
        curPos -= len;
    }
    
    @Override
    public void skipBytes(long count) {
        curPos -= count;
    }

}
