package jpabasic.entityManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaCRUD {

    /**
     * 1. EntityManagerFactory 는 어플리케이션(웹서버)이 실행될 때 딱 한번 만들어진다.
     * 2. 사용자의 하나의 요청마다 각각의 EntityManager 와 트랜잭션이 생성되며, 작업이 끝나면 종료된다.
     * 3. 단, 조회 작업은 트랜잭션 필요 x
     *
     * EntityManagerFactory 생성 -> EntityManager 생성 -> 트랜잭션 생성, 연결 -> EntityManager CRUD -> 트랜잭션 커밋, 쿼리 생성 -> EntityManager 종료 -> (반복)
     */

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        /**
         * 등록
         */
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        tx1.begin();

        try {
            Member member1 = new Member();
            member1.setId(1L);
            member1.setName("yunho1");

            Member member2 = new Member();
            member2.setId(2L);
            member2.setName("yunho2");

            em1.persist(member1);
            em1.persist(member2);
            tx1.commit(); //트랜잭션 커밋 후 쿼리 생성

        } catch (Exception e) {
            tx1.rollback();

        } finally {
            em1.close();
        }

        System.out.println("======================================");

        /**
         * 수정
         */
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();

        try {
            Member findMember = em2.find(Member.class, 1L);
            findMember.setName("yunho3");

            tx2.commit();

        } catch (Exception e) {
            tx2.rollback();

        } finally {
            em2.close();
        }

        System.out.println("======================================");

        /**
         * 조회
         */
        EntityManager em3 = emf.createEntityManager();
        EntityTransaction tx3 = em3.getTransaction();
        tx3.begin();

        try {
            List<Member> result = em3.createQuery("select m from Member m", Member.class)
                                        .getResultList(); //JPQL

            for (Member member : result) {
                System.out.println("member name = " + member.getName());
            }

            tx3.commit();

        } catch (Exception e) {
            tx3.rollback();

        } finally {
            em3.close();
        }

        System.out.println("======================================");

        /**
         * 삭제
         */
        EntityManager em4 = emf.createEntityManager();
        EntityTransaction tx4 = em4.getTransaction();
        tx4.begin();

        try {
            Member findMember = em4.find(Member.class, 2L);
            em4.remove(findMember);

            tx4.commit();

        } catch (Exception e) {
            tx4.rollback();

        } finally {
            em4.close();
        }

        emf.close();
    }
}
