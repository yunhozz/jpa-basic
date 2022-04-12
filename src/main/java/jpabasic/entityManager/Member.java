package jpabasic.entityManager;

import jpabasic.Address;
import jpabasic.RoleType;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@SequenceGenerator(name = "member_seq_generator", sequenceName = "member_seq") //시퀀스 설정
public class Member {

    /*
    <구체적인 데이터 타입 분류>
    1. 기본값 타입 : 자바 기본 타입(int, double), 래퍼 클래스(String, Long, Integer)
    2. 임베디드 타입 : JPA 에서 정의하여 사용
    3. 컬렉션 값 타입 : JPA 에서 정의하여 사용, 컬렉션에 기본값 또는 임베디드 타입을 넣은 형태
                    값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.
                    1:N 연관관계에서 cascade = ALL, orphanRemoval = true 로 설정한 것과 유사하게 동작한다.
                    정말 단순한 경우(추적 x, 업데이트 x)가 아니라면 이것을 사용하지 말고 entity 로 선언하자!
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "member_seq_generator")
    //AUTO(default & 자동 생성), IDENTITY(MySQL & 트랜잭션 커밋 이전에 쿼리 생성), SEQUENCE(Oracle), TABLE
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType; //ADMIN, USER

    @Temporal(TemporalType.TIMESTAMP) //시간 형식, 요즘은 잘 안씀 -> LocalDate(Time)
    private Date createdDate;

    private LocalDate localDate; //년, 월, 일
    private LocalDateTime localDateTime; //시간 포함

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob //용량이 큰 컨텐츠를 삽입할 때
    private String description;

    @Transient //table 에 적용 x, 메모리에서만 쓰고 싶을 때
    private int temp;

    @Embedded //임베디드 클래스 매핑
    private Address address;

    @ElementCollection
    @CollectionTable(name = "favorite_food", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "food_name") //컬럼명 지정(예외)
    private Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "address_history", joinColumns = @JoinColumn(name = "member_id"))
    private List<Address> addressHistory = new ArrayList<>();

    //기본 생성자 필요! -> @NoArgsConstructor
    protected Member() {

    }

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //getter, setter...
}
