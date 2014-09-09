package com.sequenceiq.cloudbreak.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "User.findOneWithLists",
                query = "SELECT u FROM User u "
                        + "LEFT JOIN FETCH u.stacks "
                        + "LEFT JOIN FETCH u.blueprints "
                        + "LEFT JOIN FETCH u.awsCredentials "
                        + "LEFT JOIN FETCH u.azureCredentials "
                        + "LEFT JOIN FETCH u.clusters "
                        + "WHERE u.id= :id")
})
@Table(name = "cloudbreakuser")
public class User implements ProvisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @Email
    @NotEmpty
    @Column(unique = true, nullable = false)
    private String email;

    @OneToMany(mappedBy = "awsCredentialOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AwsCredential> awsCredentials = new HashSet<>();

    @OneToMany(mappedBy = "azureCredentialOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AzureCredential> azureCredentials = new HashSet<>();

    @OneToMany(mappedBy = "gccCredentialOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GccCredential> gccCredentials = new HashSet<>();

    @NotEmpty
    private String password;

    private String confToken;

    private UserStatus status = UserStatus.PENDING;

    private Date lastLogin;

    private Date registrationDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Stack> stacks = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Blueprint> blueprints = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Cluster> clusters = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<UserRole> userRoles = new ArrayList<>();

    @ManyToOne
    private Account account;

    public User() {
    }

    public User(User user) {
        this.id = user.id;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
        this.password = user.password;
        this.confToken = user.confToken;
        this.awsCredentials = user.awsCredentials;
        this.azureCredentials = user.azureCredentials;
        this.gccCredentials = user.gccCredentials;
        this.stacks = user.stacks;
        this.blueprints = user.blueprints;
        this.clusters = user.clusters;
        this.status = user.getStatus();
        this.lastLogin = user.lastLogin;
        this.registrationDate = user.registrationDate;
        this.account = user.account;
        this.userRoles = user.userRoles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<AzureCredential> getAzureCredentials() {
        return azureCredentials;
    }

    public void setAzureCredentials(Set<AzureCredential> azureCredentials) {
        this.azureCredentials = azureCredentials;
    }

    public Set<AwsCredential> getAwsCredentials() {

        return awsCredentials;
    }

    public Set<GccCredential> getGccCredentials() {
        return gccCredentials;
    }

    public void setGccCredentials(Set<GccCredential> gccCredentials) {
        this.gccCredentials = gccCredentials;
    }

    public void setAwsCredentials(Set<AwsCredential> awsCredentials) {
        this.awsCredentials = awsCredentials;
    }

    public Set<Stack> getStacks() {
        return stacks;
    }

    public void setStacks(Set<Stack> stacks) {
        this.stacks = stacks;
    }

    public Set<Blueprint> getBlueprints() {
        return blueprints;
    }

    public void setBlueprints(Set<Blueprint> blueprints) {
        this.blueprints = blueprints;
    }

    public Set<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Set<Cluster> clusters) {
        this.clusters = clusters;
    }

    public String getConfToken() {
        return this.confToken;
    }

    public void setConfToken(String confToken) {
        this.confToken = confToken;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
