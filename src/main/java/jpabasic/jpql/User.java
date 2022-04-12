package jpabasic.jpql;

import javax.persistence.*;

@Entity
@NamedQuery(
        name = "User.findByUserName",
        query = "select u from User u where u.userName = :userName"
)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String userName;

    private int age;

    protected User() {

    }

    //연관관계 편의 메소드
    public void changeTeam(Team team) {
        this.team = team;
        team.getUsers().add(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
