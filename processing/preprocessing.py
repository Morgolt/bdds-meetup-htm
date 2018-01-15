import pandas as pd


def get_train_csv(fn, out):
    df = pd.read_csv(fn, header=False, names=['name', 'time', 'lat', 'long', 'altitude', 'speed', 'seq1', 'seq2'])
    df = df.drop(['name', 'altitude', 'seq1'], axis=1)


def add_noise(df):
    pass


def lower_diskretization_det(df):
    pass


def lower_diskretization_rand(df):
    pass


def invert_sequence(df):
    pass

