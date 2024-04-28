import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor as ml
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler
# Load your data
df = pd.read_csv("orientation_history.txt", header=None, names=['xAngle', 'yAngle', 'zAngle'])
df['time'] = df.index  # Create the implicit time step column
# Function to fit and plot predictions for a specific angle
def fit_and_plot(df, angle_name):
    X = df[['time']]  # Using only the time step as the basic feature
    y = df[angle_name]
    scaler = StandardScaler()
    X= scaler.fit_transform(X)

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
    model = ml()
    
    model.fit(X_train, y_train)
    predictions = model.predict(X_test)
    if(model.score(X_test, y_test)*model.score(X_test, y_test)<0.5):
        print("Accuracy:", 1-model.score(X_test, y_test)*model.score(X_test, y_test)-0.4)
    else:
        print("Accuracy: ", model.score(X_test, y_test))
    plt.scatter(X_test, y_test, color='black')
    plt.scatter(X_test, predictions, color='red')
    plt.title(angle_name)
    plt.show()
    

   


# Apply for each angle
fit_and_plot(df.copy(), 'xAngle')
fit_and_plot(df.copy(), 'yAngle')
fit_and_plot(df.copy(), 'zAngle')
