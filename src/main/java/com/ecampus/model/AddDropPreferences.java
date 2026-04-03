package com.ecampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "adddroppref", schema="ec2")
public class AddDropPreferences {

    @Id
    @Column(name = "adpid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adpid;

    @Column(name = "sid")
    private Long sid;

    @Column(name = "addcount")
    private Long addcount;

    @Column(name = "addp1")
    private Long addp1;

    @Column(name = "addp2")
    private Long addp2;

    @Column(name = "addp3")
    private Long addp3;

    @Column(name = "addp4")
    private Long addp4;

    @Column(name = "drop1")
    private Long drop1;

    @Column(name = "drop1_p1")
    private Long drop1_p1;

    @Column(name = "drop1_p2")
    private Long drop1_p2;

    @Column(name = "drop1_p3")
    private Long drop1_p3;

    @Column(name = "drop2")
    private Long drop2;

    @Column(name = "drop2_p1")
    private Long drop2_p1;

    @Column(name = "drop2_p2")
    private Long drop2_p2;

    @Column(name = "drop2_p3")
    private Long drop2_p3;

    @Column(name = "drop3")
    private Long drop3;

    @Column(name = "drop3_p1")
    private Long drop3_p1;

    @Column(name = "drop3_p2")
    private Long drop3_p2;

    @Column(name = "drop3_p3")
    private Long drop3_p3;

    // Relationship
    @ManyToOne
    @JoinColumn(name = "sid", referencedColumnName = "stdid", insertable = false, updatable = false)
    private Students student;

    // Getters And Setters

    public Long getAdpid(){ return adpid; }
    public void setAdpid(Long adpid) { this.adpid = adpid; }

    public Long getSid(){ return sid; }
    public void setSid(Long sid) { this.sid = sid; }

    public Long getAddcount(){ return addcount; }
    public void setAddcount(Long addcount) { this.addcount = addcount; }

    public Long getAddp1(){ return addp1; }
    public void setAddp1(Long addp1) { this.addp1 = addp1; }

    public Long getAddp2(){ return addp2; }
    public void setAddp2(Long addp2) { this.addp2 = addp2; }

    public Long getAddp3(){ return addp3; }
    public void setAddp3(Long addp3) { this.addp3 = addp3; }

    public Long getAddp4(){ return addp4; }
    public void setAddp4(Long addp4) { this.addp4 = addp4; }

    public Long getDrop1(){ return drop1; }
    public void setDrop1(Long drop1){ this.drop1 = drop1; }

    public Long getDrop1_p1(){ return drop1_p1; }
    public void setDrop1_p1(Long drop1_p1){ this.drop1_p1 = drop1_p1; }

    public Long getDrop1_p2(){ return drop1_p2; }
    public void setDrop1_p2(Long drop1_p2){ this.drop1_p2 = drop1_p2; }

    public Long getDrop1_p3(){ return drop1_p3; }
    public void setDrop1_p3(Long drop1_p3){ this.drop1_p3 = drop1_p3; }

    public Long getDrop20(){ return drop2; }
    public void setDrop2(Long drop2){ this.drop2 = drop2; }

    public Long getDrop2_p1(){ return drop2_p1; }
    public void setDrop2_p1(Long drop2_p1){ this.drop2_p1 = drop2_p1; }

    public Long getDrop2_p2(){ return drop2_p2; }
    public void setDrop2_p2(Long drop2_p2){ this.drop2_p2 = drop2_p2; }

    public Long getDrop2_p3(){ return drop2_p3; }
    public void setDrop2_p3(Long drop2_p3){ this.drop2_p3 = drop2_p3; }

    public Long getDrop3(){ return drop3; }
    public void setDrop3(Long drop3){ this.drop3 = drop3; }

    public Long getDrop3_p1(){ return drop3_p1; }
    public void setDrop3_p1(Long drop3_p1){ this.drop3_p1 = drop3_p1; }

    public Long getDrop3_p2(){ return drop3_p2; }
    public void setDrop3_p2(Long drop3_p2){ this.drop3_p2 = drop3_p2; }

    public Long getDrop3_p3(){ return drop3_p3; }
    public void setDrop3_p3(Long drop3_p3){ this.drop3_p3 = drop3_p3; }

    public Students getStudent(){ return student; }
    public void setStudent(Students student){ this.student = student; }
    
}
