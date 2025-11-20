package org.workshop.microphoneschedulerapi.domain.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
public class Personage {
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private int personageId;

    private String personageName;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "actor_id")
    private Actor actor;

    @ManyToOne
    @JoinColumn(name = "microphone_id")
    private Microphone microphone;

    @OneToMany(mappedBy = "personage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scene_character> scene_characters = new ArrayList<>();

}
