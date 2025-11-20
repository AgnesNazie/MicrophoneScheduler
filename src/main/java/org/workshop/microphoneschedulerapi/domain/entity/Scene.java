package org.workshop.microphoneschedulerapi.domain.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
//@EqualsAndHashCode
@Builder
@Entity
public class Scene {
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private int sceneId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "play_id")
    private Play play;
    private int actNumber;
    private int sceneNumber;
    private String sceneName;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scene_character> scene_characters = new ArrayList<>();

}
