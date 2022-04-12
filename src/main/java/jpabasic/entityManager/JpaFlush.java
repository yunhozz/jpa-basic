package jpabasic.entityManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaFlush {

    /**
     * 플러시(flush)
     *  1. 영속성 컨텍스트를 비우지 않음
     *  2. 영속성 컨텍스트의 변경 내용을 DB 에 동기화
     *  3. 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화 하면 됨
     */

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member(1L, "yunho");
            em.persist(member);

            System.out.println("QUERY IS SEND");
            em.flush(); //트랜잭션 커밋하기 전에 쿼리를 생성하여 DB 에 바로 날려준다

            System.out.println("COMMIT START");
            tx.commit();

        } catch (Exception e) {
            tx.rollback();

        } finally {
            em.close();
        }

        emf.close();
    }
}
