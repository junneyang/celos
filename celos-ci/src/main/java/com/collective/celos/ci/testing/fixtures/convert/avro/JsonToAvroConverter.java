package com.collective.celos.ci.testing.fixtures.convert.avro;

import com.collective.celos.ci.mode.test.TestRun;
import com.collective.celos.ci.testing.fixtures.create.FixObjectCreator;
import com.collective.celos.ci.testing.structure.fixobject.AbstractFixFileConverter;
import com.collective.celos.ci.testing.structure.fixobject.FixFile;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

import java.io.*;

/**
 * Created by akonopko on 9/29/14.
 */
public class JsonToAvroConverter extends AbstractFixFileConverter {

    private final FixObjectCreator<FixFile> schemaCreator;

    public JsonToAvroConverter(FixObjectCreator<FixFile> schemaCreator) {
        this.schemaCreator = schemaCreator;
    }

    @Override
    public FixFile convert(TestRun tr, FixFile ff) throws Exception {
        Schema schema = new Schema.Parser().parse(schemaCreator.create(tr).getContent());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream input = ff.getContent();
        DataFileWriter<Object> writer;;
        try {
            DatumReader<Object> reader = new GenericDatumReader<>(schema);
            DataInputStream din = new DataInputStream(input);
            writer = new DataFileWriter<>(new GenericDatumWriter<>());
            writer.create(schema, baos);
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, din);
            Object datum;
            while (true) {
                try {
                    datum = reader.read(null, decoder);
                } catch (EOFException eofe) {
                    break;
                }
                writer.append(datum);
            }
            writer.flush();
        } finally {
            input.close();
        }
        return new FixFile(new ByteArrayInputStream(baos.toByteArray()));
    }


}