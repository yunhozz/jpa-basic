package jpabasic.entityManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaPersistence {

    /**
     * 애플리케이션과 DB 사이에 가상의 영속성 컨텍스트가 존재 (자바 힙 메모리에 존재)
     *
     *  1. 1차 캐시 (= 영속성 컨텍스트)
     *  2. 동일성(identity) 보장
     *  3. 트랜잭션을 지원하는 쓰기 지연
     *  4. 변경 감지(Dirty Checking)
     *  5. 지연 로딩(Lazy Loading)
     */

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        /**
         * 1차 캐시 저장, 조회 & 영속 엔티티 동일성 보장
         */
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        tx1.begin();

        try {
            //비영속
            Member member = new Member();
            member.setId(1L);
            member.setName("HelloJPA");

            //영속
            System.out.println("==== BEFORE ====");
            em1.persist(member); //아무런 표시 x, 1차 캐시에 저장 -> DB 저장 x
            System.out.println("==== AFTER ====");

            Member findMember1 = em1.find(Member.class, 1L); //select 쿼리 x : 1차 캐시에 저장되었기 때문
            Member findMember2 = em1.find(Member.class, 2L); //select 쿼리 o : 1차 캐시에 없어서 DB 조회 후 캐시에 저장, 반환

            Member a = em1.find(Member.class, 1L);
            Member b = em1.find(Member.class, 1L);
            System.out.println(a == b); //영속 엔티티의 동일성 보장 (true)

            tx1.commit(); //트랜잭션 커밋 이후에 비로소 DB 에 저장됨!!!! (INSERT SQL 쿼리 생성)

        } catch (Exception e) {
            tx1.rollback();

        } finally {
            em1.close();
        }

        System.out.println("==================================");

        /**
         * 트랜잭션을 지원하는 쓰기 지연
         */
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();

        try {
            Member member1 = new Member(3L, "A");
            Member member2 = new Member(4L, "B");

            //버퍼링을 모아서 write -> 성능상의 이점을 얻을 수 있다.
            em2.persist(member1);
            em2.persist(member2);

            tx2.commit();

        } catch (Exception e) {
            tx2.rollback();

        } finally {
            em2.close();
        }

        System.out.println("==================================");

        /**
         * 변경 감지 (Dirty Checking)
         */
        EntityManager em3 = emf.createEntityManager();
        EntityTransaction tx3 = em3.getTransaction();
        tx3.begin();

        try {
            Member member = em3.find(Member.class, 3L); //영속 엔티티 조회
            member.setName("yunho"); //영속 엔티티 데이터 수정

            tx3.commit(); //UPDATE 쿼리를 포함한다 -> em.update(member) 필요 x

        } catch (Exception e) {
            tx3.rollback();

        } finally {
            em3.close();
        }

        emf.close();
    }
}
