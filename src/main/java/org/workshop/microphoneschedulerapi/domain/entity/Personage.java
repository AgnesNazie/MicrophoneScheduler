package org.workshop.microphoneschedulerapi.domain.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    //@Column(nullable = false, unique = true)
    private int personageId;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "actor_id")
    private Actor actor;

    @ManyToOne
    @JoinColumn(name = "microphone_id")
    private Microphone microphone;

    public Microphone getMicrophone() {
        return microphone;
    }

    public void setMicrophone(Microphone microphone) {
        this.microphone = microphone;
    }

    //@JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "personage", fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private List<Scene_character> scene_characters;
    @Column(unique = true)
    private String personageName;
}
