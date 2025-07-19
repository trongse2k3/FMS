package com.example.fms.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static final String TAG = "UserManager";
    private static final String USERS_COLLECTION = "users";
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    
    public UserManager() {
        firebaseAuth = FirebaseManager.getInstance().getAuth();
        firestore = FirebaseManager.getInstance().getFirestore();
    }
    
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
    
    public interface UserDataCallback {
        void onSuccess(Map<String, Object> userData);
        void onFailure(Exception e);
    }
    
    // Đăng ký người dùng mới
    public void registerUser(String email, String password, String username, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        
                        // Tạo thông tin người dùng trong Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("username", username);
                        userData.put("createdAt", System.currentTimeMillis());
                        
                        // Không thêm thông tin mặc định về đội bóng ở đây, sẽ được thêm từ CreateActivity
                        
                        firestore.collection(USERS_COLLECTION)
                            .document(user.getUid())
                            .set(userData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "User profile created");
                                    callback.onSuccess(user);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error creating user profile", e);
                                    callback.onFailure(e);
                                }
                            });
                    } else {
                        Log.e(TAG, "Registration failed", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Đăng nhập người dùng
    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Lấy thông tin người dùng
    public void getUserData(String userId, UserDataCallback callback) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userData = document.getData();
                            callback.onSuccess(userData);
                        } else {
                            Log.d(TAG, "No such document");
                            callback.onFailure(new Exception("User not found"));
                        }
                    } else {
                        Log.e(TAG, "Error getting user data", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Cập nhật thông tin người dùng
    public void updateUserData(String userId, Map<String, Object> userData, UserDataCallback callback) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(userData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "User data updated successfully");
                    callback.onSuccess(userData);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error updating user data", e);
                    callback.onFailure(e);
                }
            });
    }
    
    // Cập nhật tên đội bóng
    public void updateTeamName(String userId, String teamName, UserDataCallback callback) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("teamName", teamName);
        
        updateUserData(userId, updateData, callback);
    }
    
    // Cập nhật ID logo đội bóng
    public void updateTeamLogo(String userId, int logoId, UserDataCallback callback) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("teamLogoId", logoId);
        
        updateUserData(userId, updateData, callback);
    }
    
    // Cập nhật số tiền
    public void updateMoney(String userId, int money, UserDataCallback callback) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("money", money);
        
        updateUserData(userId, updateData, callback);
    }

    // Thêm tiền cho người dùng
    public void addMoney(String userId, int amount, UserDataCallback callback) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (snapshot.exists()) {
                // Safely get money, default to 0 if not present
                Long currentMoneyLong = snapshot.getLong("money");
                long currentMoney = (currentMoneyLong != null) ? currentMoneyLong : 0L;
                long newMoney = currentMoney + amount;
                transaction.update(userRef, "money", newMoney);
            }
            // If snapshot doesn't exist, transaction will fail and trigger onFailure
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Money added successfully for user " + userId);
            // Create a map with the new balance if needed, though the transaction was successful
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("status", "success");
            callback.onSuccess(resultData);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding money for user " + userId, e);
            callback.onFailure(e);
        });
    }
    
    // Đồng bộ dữ liệu đội bóng
    public void syncTeamData(String userId, String teamName, int logoId, int money, UserDataCallback callback) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("teamName", teamName);
        updateData.put("teamLogoId", logoId);
        updateData.put("money", money);
        
        updateUserData(userId, updateData, callback);
    }
    
    // Kiểm tra xem người dùng đã có đội bóng chưa
    public void hasTeamData(String userId, UserDataCallback callback) {
        getUserData(userId, new UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                boolean hasTeam = userData.containsKey("teamName") && userData.containsKey("teamLogoId");
                Map<String, Object> result = new HashMap<>();
                result.put("hasTeam", hasTeam);
                callback.onSuccess(result);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    // Đăng xuất người dùng
    public void logoutUser() {
        firebaseAuth.signOut();
    }
    
    // Kiểm tra trạng thái đăng nhập
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    // Lấy ID người dùng hiện tại
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    // Lấy đối tượng FirebaseUser hiện tại
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
} 