package com.arknightsinfrastructurestationbackend.entitiy.workFile.adapter;

public interface WorkFileInterface {

    Long getId();
    void setId(Long id);

    String getAuthor();
    void setAuthor(String author);

    Long getAuthorId();
    void setAuthorId(Long authorId);

    String getName();
    void setName(String name);

    String getType();
    void setType(String type);

    String getLayout();
    void setLayout(String layout);

    String getDescription();
    void setDescription(String description);

    String getDescriptionPictures();
    void setDescriptionPictures(String descriptionPictures);

    String getStorageType();
    void setStorageType(String storageType);

    String getFileContent();
    void setFileContent(String fileContent);

    String getFileRequest();
    void setFileRequest(String fileRequest);
}

