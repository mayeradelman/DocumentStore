package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    File baseDir;

    public DocumentPersistenceManager(File baseDir){
        if(baseDir == null){
            baseDir = new File(System.getProperty("user.dir"));
        }
        this.baseDir = baseDir;
        if(!baseDir.exists()){
            this.baseDir.mkdirs();
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        JsonSerializer<Document> documentSerializer = (Document document, Type type, JsonSerializationContext jsonSerializationContext) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uri", String.valueOf(document.getKey()));
            jsonObject.addProperty("text", document.getDocumentAsTxt());
            jsonObject.addProperty("textHashCode", document.hashCode());
            Gson gson = new Gson();
            jsonObject.addProperty("wordMap", gson.toJson(document.getWordMap()));
            return jsonObject;
        };
        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                if (field.getName().equals("pdfData")) {
                    return true;
                }
                if (field.getName().equals("words")) {
                    return true;
                }
                if (field.getName().equals("lastUsedTime")) {
                    return true;
                }
                return false;
            }
            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        Gson gson = new GsonBuilder().addSerializationExclusionStrategy(strategy).registerTypeAdapter(Document.class, documentSerializer).setPrettyPrinting().create();
        String jsonFilePath = this.baseDir + uri.toString().replace("/", File.separator).replaceFirst(uri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
        File jsonFile = new File(jsonFilePath);
        File jsonParentDirectory = new File(jsonFile.getParent());
        jsonParentDirectory.mkdirs();
        jsonFile.createNewFile();
        FileWriter fileWriter = new FileWriter(jsonFilePath);
        fileWriter.write(gson.toJson(val));
        fileWriter.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        String jsonFilePath = this.baseDir + uri.toString().replace("/", File.separator).replaceFirst(uri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
        File jsonFile = new File(jsonFilePath);
        if(!jsonFile.exists()) {
            throw new FileNotFoundException();
        }
        JsonDeserializer<Document> documentDeserializer = (JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) -> {
            String text = jsonElement.getAsJsonObject().get("text").getAsString();
            int textHashCode = jsonElement.getAsJsonObject().get("textHashCode").getAsInt();
            Gson gson = new Gson();
            JsonElement wordMapString = jsonElement.getAsJsonObject().get("wordMap");
            Type hashMapType = new TypeToken<HashMap<String, Integer>>(){}.getType();
            HashMap<String, Integer> wordMap = gson.fromJson(wordMapString, hashMapType);
            return new DocumentImpl(uri, text, textHashCode, wordMap);
        };
        Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, documentDeserializer).create();
        JsonReader reader = new JsonReader(new FileReader(jsonFilePath));
        Type type = new TypeToken<Document>(){}.getType();
        Document document = gson.fromJson(reader,type);
        reader.close();
        this.cleanUp(jsonFile);
        return document;
    }
    private void cleanUp(File file) {
        if(!file.equals(this.baseDir)) {
            if (file.delete()) {
                this.cleanUp(file.getParentFile());
            }
        }
    }
}

