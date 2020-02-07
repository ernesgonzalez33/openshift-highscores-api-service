package io.nub3s;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;

// import javax.json.bind.annotation.JsonbProperty;

@MongoEntity(collection="scores")
public class Score extends PanacheMongoEntity {
    public String name;
    public Integer score;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name.toUpperCase(); // store all names in uppercase in the DB
    }

    public Integer getScore(){
        return score;
    }

    public void setScore(Integer score){
        this.score = score;
    }

    public static List<Score> findTopTen(){
        PanacheQuery<Score> allNonZeroScores = findAll(Sort.descending("score"));
        allNonZeroScores.page(Page.ofSize(10));
        return allNonZeroScores.list();
    }

    public static List<Score> listAllDescending(){
        return listAll(Sort.descending("score"));
    }

    @Override
    public String toString() {
        return "{" +
            "\"name\":\"" + name + "\"," +
            "\"score\":" + score + 
            "}";
    }
}
